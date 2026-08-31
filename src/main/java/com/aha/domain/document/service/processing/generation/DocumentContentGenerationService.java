package com.aha.domain.document.service.processing.generation;

import com.aha.domain.document.service.processing.generation.model.TopicContentSource;
import com.aha.domain.document.service.processing.generation.model.persistence.DocumentContentGenerationPersistenceService;
import com.aha.domain.learningnote.client.generation.ConceptExplanationClient;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.domain.learningnote.client.generation.dto.ConceptSourceChunk;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentContentGenerationService {

    private final DocumentContentGenerationSourceService sourceService;
    private final ConceptExplanationClient conceptExplanationClient;
    private final DocumentContentGenerationPersistenceService persistenceService;

    public int generate(Long learningNoteId) {
        List<TopicContentSource> topicSources = sourceService.getTopicSources(learningNoteId);
        if (topicSources.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND);
        }

        for (TopicContentSource source : topicSources) {
            ConceptExplanationResult result = conceptExplanationClient.generate(
                    toGenerationRequest(source)
            );
            persistenceService.saveGeneratedContent(learningNoteId, source, result);
        }

        log.info("학습노트 개념 설명 생성 완료. learningNoteId={}, topicCount={}",
                learningNoteId, topicSources.size());
        return topicSources.size();
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
