package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.client.mapping.DocumentScopeMappingClient;
import com.aha.domain.document.client.mapping.dto.ChunkScopeMappingRequest;
import com.aha.domain.document.client.mapping.dto.ScopeMappingAiResult;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.openai.OpenAiApiExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class DocumentScopeMappingBatchProcessor {

    private final DocumentScopeMappingClient mappingClient;
    private final Executor scopeMappingExecutor;
    private final int batchSize;

    public DocumentScopeMappingBatchProcessor(
            DocumentScopeMappingClient mappingClient,
            @Qualifier("documentScopeMappingTaskExecutor")
            Executor scopeMappingExecutor,
            @Value("${document.processing.scope-mapping-batch-size:8}")
            int batchSize
    ) {
        this.mappingClient = mappingClient;
        this.scopeMappingExecutor = scopeMappingExecutor;
        this.batchSize = Math.max(1, batchSize);
    }

    public List<ScopeMappingAiResult> process(
            List<ChunkScopeMappingRequest> mappingRequests
    ) {
        if (mappingRequests == null || mappingRequests.isEmpty()) {
            return List.of();
        }

        List<MappingBatch> batches = createBatches(mappingRequests);
        long allBatchesStartedAt = System.nanoTime();
        AtomicReference<BusinessException> fatalFailure = new AtomicReference<>();

        List<CompletableFuture<List<ScopeMappingAiResult>>> tasks = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(
                        () -> requestBatchSafely(batch, fatalFailure),
                        scopeMappingExecutor
                ))
                .toList();

        List<ScopeMappingAiResult> results = new ArrayList<>();
        for (CompletableFuture<List<ScopeMappingAiResult>> task : tasks) {
            results.addAll(task.join());
        }

        if (fatalFailure.get() != null) {
            throw fatalFailure.get();
        }

        log.info(
                "[DOCUMENT_PERF] parallel scope mapping AI batches completed. batchCount={}, requestCount={}, resultCount={}, elapsedMs={}",
                batches.size(),
                mappingRequests.size(),
                results.size(),
                elapsedMillis(allBatchesStartedAt)
        );

        return List.copyOf(results);
    }

    private List<ScopeMappingAiResult> requestBatchSafely(
            MappingBatch batch,
            AtomicReference<BusinessException> fatalFailure
    ) {
        if (fatalFailure.get() != null) {
            return List.of();
        }

        try {
            return requestBatch(batch);
        } catch (BusinessException exception) {
            if (OpenAiApiExceptionTranslator.isCreditExhausted(exception)) {
                fatalFailure.compareAndSet(null, exception);
                return List.of();
            }
            throw exception;
        }
    }

    private List<MappingBatch> createBatches(
            List<ChunkScopeMappingRequest> mappingRequests
    ) {
        List<MappingBatch> batches = new ArrayList<>();
        for (int start = 0; start < mappingRequests.size(); start += batchSize) {
            int end = Math.min(start + batchSize, mappingRequests.size());
            batches.add(new MappingBatch(
                    start,
                    end,
                    List.copyOf(mappingRequests.subList(start, end))
            ));
        }
        return List.copyOf(batches);
    }

    private List<ScopeMappingAiResult> requestBatch(MappingBatch batch) {
        int candidateCount = batch.requests().stream()
                .mapToInt(request -> request.scopeCandidates() == null
                        ? 0
                        : request.scopeCandidates().size())
                .sum();

        log.info(
                "문서 목차 AI rerank 요청. start={}, end={}, chunkCount={}, candidateCount={}, thread={}",
                batch.start(),
                batch.end(),
                batch.requests().size(),
                candidateCount,
                Thread.currentThread().getName()
        );

        long requestStartedAt = System.nanoTime();
        List<ScopeMappingAiResult> batchResults = mappingClient.mapChunks(batch.requests());
        if (batchResults == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        log.info(
                "[DOCUMENT_PERF] scope mapping AI batch completed. start={}, end={}, chunkCount={}, candidateCount={}, resultCount={}, elapsedMs={}, thread={}",
                batch.start(),
                batch.end(),
                batch.requests().size(),
                candidateCount,
                batchResults.size(),
                elapsedMillis(requestStartedAt),
                Thread.currentThread().getName()
        );

        batchResults.forEach(result -> log.info(
                "[AI MAPPING RESULT] chunkId={}, scopeNodeId={}, confidence={}, reason={}",
                result.documentChunkId(),
                result.examScopeNodeId(),
                result.confidenceScore(),
                result.mappingReason()
        ));

        return List.copyOf(batchResults);
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    private record MappingBatch(
            int start,
            int end,
            List<ChunkScopeMappingRequest> requests
    ) {
    }
}
