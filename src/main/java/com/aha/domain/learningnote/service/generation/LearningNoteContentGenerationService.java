package com.aha.domain.learningnote.service.generation;

import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.document.repository.projection.MappedTopicChunkProjection;
import com.aha.domain.learningnote.client.generation.ConceptExplanationClient;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.domain.learningnote.client.generation.dto.ConceptSourceChunk;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningNoteContentGenerationService {

    private static final int MAX_SOURCE_CHARACTERS_PER_TOPIC = 40_000;

    private final DocumentScopeMappingRepository documentScopeMappingRepository;
    private final ConceptExplanationClient conceptExplanationClient;
    private final LearningNoteContentPersistenceService persistenceService;

    public void generate(Long learningNoteId) {
        if (learningNoteId == null || learningNoteId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<MappedTopicChunkProjection> mappedChunks =
                documentScopeMappingRepository.findMappedTopicChunks(learningNoteId);

        if (mappedChunks == null || mappedChunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND);
        }

        Map<Long, TopicSources> sourcesByTopic = groupByTopic(mappedChunks);
        List<GeneratedConceptExplanation> explanations = new ArrayList<>(sourcesByTopic.size());

        for (TopicSources topicSources : sourcesByTopic.values()) {
            ConceptExplanationResult result = conceptExplanationClient.generate(
                    new ConceptExplanationRequest(
                            topicSources.topicId(),
                            topicSources.topicTitle(),
                            List.copyOf(topicSources.chunks())
                    )
            );

            explanations.add(new GeneratedConceptExplanation(
                    topicSources.topicId(),
                    result.title(),
                    result.content()
            ));
        }

        if (explanations.isEmpty()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }

        persistenceService.replaceDocumentBasedContents(learningNoteId, explanations);

        log.info(
                "학습노트 목차별 개념 설명 생성 완료. learningNoteId={}, topicCount={}",
                learningNoteId,
                explanations.size()
        );
    }

    public void generateTopic(Long learningNoteId, Long examScopeNodeId) {
        if (learningNoteId == null || learningNoteId <= 0
                || examScopeNodeId == null || examScopeNodeId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<MappedTopicChunkProjection> mappedChunks = documentScopeMappingRepository
                .findMappedTopicChunks(learningNoteId)
                .stream()
                .filter(chunk -> examScopeNodeId.equals(chunk.getTopicId()))
                .toList();

        Map<Long, TopicSources> sourcesByTopic = groupByTopic(mappedChunks);
        TopicSources topicSources = sourcesByTopic.get(examScopeNodeId);
        if (topicSources == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_NOT_FOUND);
        }

        ConceptExplanationResult result = conceptExplanationClient.generate(
                new ConceptExplanationRequest(
                        topicSources.topicId(),
                        topicSources.topicTitle(),
                        List.copyOf(topicSources.chunks())
                )
        );

        persistenceService.saveDocumentBasedContent(
                learningNoteId,
                new GeneratedConceptExplanation(
                        examScopeNodeId,
                        result.title(),
                        result.content()
                )
        );
    }

    private Map<Long, TopicSources> groupByTopic(List<MappedTopicChunkProjection> mappedChunks) {
        Map<Long, TopicSources> sourcesByTopic = new LinkedHashMap<>();

        for (MappedTopicChunkProjection mappedChunk : mappedChunks) {
            if (mappedChunk == null
                    || mappedChunk.getTopicId() == null
                    || mappedChunk.getTopicTitle() == null
                    || mappedChunk.getTopicTitle().isBlank()
                    || mappedChunk.getChunkId() == null
                    || mappedChunk.getChunkText() == null
                    || mappedChunk.getChunkText().isBlank()) {
                continue;
            }

            TopicSources topic = sourcesByTopic.computeIfAbsent(
                    mappedChunk.getTopicId(),
                    ignored -> new TopicSources(
                            mappedChunk.getTopicId(),
                            mappedChunk.getTopicTitle().trim(),
                            new ArrayList<>(),
                            0
                    )
            );

            if (topic.characterCount() >= MAX_SOURCE_CHARACTERS_PER_TOPIC) {
                continue;
            }

            String chunkText = mappedChunk.getChunkText().trim();
            int remaining = MAX_SOURCE_CHARACTERS_PER_TOPIC - topic.characterCount();
            String boundedText = chunkText.length() <= remaining
                    ? chunkText
                    : chunkText.substring(0, remaining);

            if (!boundedText.isBlank()) {
                topic.chunks().add(new ConceptSourceChunk(
                        mappedChunk.getChunkId(),
                        mappedChunk.getChunkOrder(),
                        mappedChunk.getChunkType() == null ? null : mappedChunk.getChunkType().name(),
                        boundedText
                ));
                topic.addCharacters(boundedText.length());
            }
        }

        sourcesByTopic.entrySet().removeIf(entry -> entry.getValue().chunks().isEmpty());
        return sourcesByTopic;
    }

    private static final class TopicSources {
        private final Long topicId;
        private final String topicTitle;
        private final List<ConceptSourceChunk> chunks;
        private int characterCount;

        private TopicSources(
                Long topicId,
                String topicTitle,
                List<ConceptSourceChunk> chunks,
                int characterCount
        ) {
            this.topicId = topicId;
            this.topicTitle = topicTitle;
            this.chunks = chunks;
            this.characterCount = characterCount;
        }

        private Long topicId() { return topicId; }
        private String topicTitle() { return topicTitle; }
        private List<ConceptSourceChunk> chunks() { return chunks; }
        private int characterCount() { return characterCount; }
        private void addCharacters(int count) { characterCount += count; }
    }
}
