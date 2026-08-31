package com.aha.domain.document.service.processing.generation;

import com.aha.domain.document.service.processing.generation.model.TopicContentSource;
import com.aha.domain.document.config.DocumentProcessingProperties;
import com.aha.domain.document.service.processing.generation.model.persistence.DocumentContentGenerationPersistenceService;
import com.aha.domain.learningnote.client.generation.ConceptExplanationClient;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.domain.learningnote.client.generation.dto.ConceptSourceChunk;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationBatchItem;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.openai.OpenAiApiExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentContentGenerationService {

    private final DocumentContentGenerationSourceService sourceService;
    private final ConceptExplanationClient conceptExplanationClient;
    private final DocumentContentGenerationPersistenceService persistenceService;
    private final Executor contentGenerationExecutor;
    private final int batchSize;
    private final int concurrency;
    private final int maxOutputTokens;

    public DocumentContentGenerationService(
            DocumentContentGenerationSourceService sourceService,
            ConceptExplanationClient conceptExplanationClient,
            DocumentContentGenerationPersistenceService persistenceService,
            @Qualifier("documentContentGenerationTaskExecutor")
            Executor contentGenerationExecutor,
            DocumentProcessingProperties processingProperties
    ) {
        this.sourceService = sourceService;
        this.conceptExplanationClient = conceptExplanationClient;
        this.persistenceService = persistenceService;
        this.contentGenerationExecutor = contentGenerationExecutor;
        this.batchSize = Math.max(1, processingProperties.getContentGenerationBatchSize());
        this.concurrency = Math.max(1, processingProperties.getContentGenerationConcurrency());
        this.maxOutputTokens = Math.max(
                256,
                processingProperties.getContentGenerationMaxOutputTokens()
        );
    }

    public int generate(Long learningNoteId) {
        List<TopicContentSource> topicSources = sourceService.getTopicSources(learningNoteId);
        if (topicSources.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND);
        }

        long generationStartedAt = System.nanoTime();
        List<List<TopicContentSource>> batches = createBatches(topicSources);
        List<List<TopicContentSource>> failedBatches =
                Collections.synchronizedList(new ArrayList<>());
        AtomicReference<BusinessException> fatalFailure = new AtomicReference<>();
        List<CompletableFuture<Void>> generationTasks = batches.stream()
                .map(batch -> CompletableFuture.runAsync(
                        () -> generateAndCollectFailure(
                                learningNoteId,
                                batch,
                                failedBatches,
                                fatalFailure
                        ),
                        contentGenerationExecutor
                ))
                .toList();

        CompletableFuture.allOf(
                generationTasks.toArray(CompletableFuture[]::new)
        ).join();

        if (fatalFailure.get() != null) {
            throw fatalFailure.get();
        }

        recoverFailedBatches(learningNoteId, failedBatches);

        log.info(
                "[DOCUMENT_PERF] parallel topic generation completed. learningNoteId={}, topicCount={}, batchCount={}, batchSize={}, concurrency={}, maxOutputTokens={}, elapsedMs={}",
                learningNoteId,
                topicSources.size(),
                batches.size(),
                batchSize,
                concurrency,
                maxOutputTokens,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - generationStartedAt)
        );

        log.info("학습노트 개념 설명 생성 완료. learningNoteId={}, topicCount={}",
                learningNoteId, topicSources.size());
        return topicSources.size();
    }

    private void generateAndCollectFailure(
            Long learningNoteId,
            List<TopicContentSource> batch,
            List<List<TopicContentSource>> failedBatches,
            AtomicReference<BusinessException> fatalFailure
    ) {
        if (fatalFailure.get() != null) {
            return;
        }
        try {
            generateAndSaveBatch(learningNoteId, batch);
        } catch (RuntimeException exception) {
            if (exception instanceof BusinessException businessException
                    && OpenAiApiExceptionTranslator.isCreditExhausted(businessException)) {
                fatalFailure.compareAndSet(null, businessException);
                return;
            }
            failedBatches.add(batch);
            log.warn(
                    "[DOCUMENT_PERF] content generation deferred for serial recovery. learningNoteId={}, scopeNodeIds={}, thread={}",
                    learningNoteId,
                    batch.stream().map(TopicContentSource::scopeNodeId).toList(),
                    Thread.currentThread().getName(),
                    exception
            );
        }
    }

    private void recoverFailedBatches(
            Long learningNoteId,
            List<List<TopicContentSource>> failedBatches
    ) {
        if (failedBatches.isEmpty()) {
            return;
        }

        long recoveryStartedAt = System.nanoTime();
        log.warn(
                "[DOCUMENT_PERF] serial content generation recovery started. learningNoteId={}, failedBatchCount={}",
                learningNoteId,
                failedBatches.size()
        );

        for (List<TopicContentSource> failedBatch : List.copyOf(failedBatches)) {
            generateAndSaveBatch(learningNoteId, failedBatch);
        }

        log.info(
                "[DOCUMENT_PERF] serial content generation recovery completed. learningNoteId={}, recoveredBatchCount={}, elapsedMs={}",
                learningNoteId,
                failedBatches.size(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - recoveryStartedAt)
        );
    }

    public void finalizeGeneration(Long learningNoteId) {
        persistenceService.markLearningNoteReady(learningNoteId);
    }

    public void generateTopic(Long learningNoteId, Long scopeNodeId) {
        TopicContentSource source = sourceService
                .getTopicSource(learningNoteId, scopeNodeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND
                ));

        ConceptExplanationResult result = conceptExplanationClient.generate(
                toGenerationRequest(source)
        );
        persistenceService.saveGeneratedContent(learningNoteId, source, result);

        log.info("단원별 개념 설명 생성 완료. learningNoteId={}, scopeNodeId={}",
                learningNoteId, scopeNodeId);
    }

    private void generateAndSaveTopic(
            Long learningNoteId,
            TopicContentSource source
    ) {
        long topicStartedAt = System.nanoTime();
        ConceptExplanationResult result = conceptExplanationClient.generate(
                toGenerationRequest(source)
        );
        persistenceService.saveGeneratedContent(learningNoteId, source, result);

        log.info(
                "[DOCUMENT_PERF] topic generation completed. learningNoteId={}, scopeNodeId={}, chunkCount={}, elapsedMs={}, thread={}",
                learningNoteId,
                source.scopeNodeId(),
                source.chunks().size(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - topicStartedAt),
                Thread.currentThread().getName()
        );
    }

    private void generateAndSaveBatch(
            Long learningNoteId,
            List<TopicContentSource> sources
    ) {
        if (sources.size() == 1) {
            generateAndSaveTopic(learningNoteId, sources.get(0));
            return;
        }

        long batchStartedAt = System.nanoTime();
        List<ConceptExplanationRequest> requests = sources.stream()
                .map(this::toGenerationRequest)
                .toList();

        try {
            List<ConceptExplanationBatchItem> results =
                    conceptExplanationClient.generateBatch(requests);
            Map<Long, TopicContentSource> sourceByScopeNodeId = sources.stream()
                    .collect(Collectors.toMap(
                            TopicContentSource::scopeNodeId,
                            Function.identity()
                    ));

            for (ConceptExplanationBatchItem item : results) {
                TopicContentSource source = sourceByScopeNodeId.get(item.examScopeNodeId());
                if (source == null) {
                    throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
                }
                persistenceService.saveGeneratedContent(
                        learningNoteId,
                        source,
                        new ConceptExplanationResult(item.title(), item.content())
                );
            }

            log.info(
                    "[DOCUMENT_PERF] topic generation batch completed. learningNoteId={}, topicCount={}, elapsedMs={}, thread={}",
                    learningNoteId,
                    sources.size(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - batchStartedAt),
                    Thread.currentThread().getName()
            );
        } catch (BusinessException exception) {
            log.warn(
                    "토픽 배치 생성 실패로 단일 생성으로 전환합니다. learningNoteId={}, topicCount={}",
                    learningNoteId,
                    sources.size(),
                    exception
            );
            sources.forEach(source -> generateAndSaveTopic(learningNoteId, source));
        }
    }

    private List<List<TopicContentSource>> createBatches(
            List<TopicContentSource> sources
    ) {
        List<List<TopicContentSource>> batches = new ArrayList<>();
        for (int start = 0; start < sources.size(); start += batchSize) {
            int end = Math.min(start + batchSize, sources.size());
            batches.add(List.copyOf(sources.subList(start, end)));
        }
        return List.copyOf(batches);
    }

    private ConceptExplanationRequest toGenerationRequest(TopicContentSource source) {
        List<ConceptSourceChunk> chunks = source.chunks().stream()
                .map(chunk -> new ConceptSourceChunk(
                        chunk.documentChunkId(),
                        chunk.chunkOrder(),
                        chunk.contentType(),
                        chunk.contentText()
                ))
                .toList();

        return new ConceptExplanationRequest(
                source.scopeNodeId(),
                source.scopeTitle(),
                chunks
        );
    }
}
