package com.aha.domain.document.service.processing.generation;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.document.service.processing.generation.model.TopicContentSource;
import com.aha.domain.document.service.processing.generation.model.TopicSourceChunk;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentContentGenerationSourceService {

    private final DocumentScopeMappingRepository
            documentScopeMappingRepository;

    public List<TopicContentSource> getTopicSources(
            Long learningNoteId
    ) {

        validateLearningNoteId(
                learningNoteId
        );

        List<DocumentScopeMapping> mappings =
                documentScopeMappingRepository
                        .findAllForContentGeneration(
                                learningNoteId
                        );

        if (mappings.isEmpty()) {
            return List.of();
        }

        Map<Long, TopicSourceAccumulator> grouped =
                new LinkedHashMap<>();

        for (DocumentScopeMapping mapping : mappings) {

            if (mapping == null) {
                continue;
            }

            DocumentChunk chunk =
                    mapping.getDocumentChunk();

            ExamScopeNode scopeNode =
                    mapping.getExamScopeNode();

            if (chunk == null
                    || chunk.getId() == null
                    || scopeNode == null
                    || scopeNode.getId() == null) {

                continue;
            }

            TopicSourceAccumulator accumulator =
                    grouped.computeIfAbsent(
                            scopeNode.getId(),
                            ignored ->
                                    new TopicSourceAccumulator(
                                            scopeNode
                                    )
                    );

            accumulator.addChunk(
                    chunk
            );
        }

        return grouped.values()
                .stream()
                .map(
                        TopicSourceAccumulator::toSource
                )
                .toList();
    }

    public Optional<TopicContentSource> getTopicSource(
            Long learningNoteId,
            Long scopeNodeId
    ) {
        validateLearningNoteId(learningNoteId);
        if (scopeNodeId == null || scopeNodeId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return getTopicSources(learningNoteId).stream()
                .filter(source -> source.scopeNodeId().equals(scopeNodeId))
                .findFirst()
                .filter(source -> !source.chunks().isEmpty());
    }

    private void validateLearningNoteId(
            Long learningNoteId
    ) {

        if (learningNoteId == null
                || learningNoteId <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private static class TopicSourceAccumulator {

        private final ExamScopeNode scopeNode;

        private final Map<Long, TopicSourceChunk> chunks =
                new LinkedHashMap<>();

        private TopicSourceAccumulator(
                ExamScopeNode scopeNode
        ) {
            this.scopeNode = scopeNode;
        }

        private void addChunk(
                DocumentChunk chunk
        ) {

            if (chunk.getId() == null
                    || chunk.getContentText() == null
                    || chunk.getContentText().isBlank()) {

                return;
            }

            chunks.putIfAbsent(
                    chunk.getId(),
                    new TopicSourceChunk(
                            chunk.getId(),
                            chunk.getChunkOrder(),
                            chunk.getSectionTitle(),
                            chunk.getContentType().name(),
                            chunk.getContentText()
                    )
            );
        }

        private TopicContentSource toSource() {

            return new TopicContentSource(
                    scopeNode.getId(),
                    scopeNode.getCode(),
                    scopeNode.getTitle(),
                    new ArrayList<>(
                            chunks.values()
                    )
            );
        }
    }
}
