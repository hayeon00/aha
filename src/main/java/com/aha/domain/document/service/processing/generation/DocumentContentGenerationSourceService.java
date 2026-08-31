package com.aha.domain.document.service.processing.generation;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.config.DocumentProcessingProperties;
import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.document.service.processing.generation.model.TopicContentSource;
import com.aha.domain.document.service.processing.generation.model.TopicSourceChunk;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentContentGenerationSourceService {

    private final DocumentScopeMappingRepository
            documentScopeMappingRepository;
    private final DocumentProcessingProperties processingProperties;

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
                                            scopeNode,
                                            processingProperties.getContentGenerationMaxSourceChunks(),
                                            processingProperties.getContentGenerationMaxSourceCharacters()
                                    )
                    );

            accumulator.addMapping(mapping);
        }

        List<TopicContentSource> sources = grouped.values()
                .stream()
                .map(
                        TopicSourceAccumulator::toSource
                )
                .toList();

        int totalSourceChunks = sources.stream()
                .mapToInt(source -> source.chunks().size())
                .sum();
        int totalSourceCharacters = sources.stream()
                .flatMap(source -> source.chunks().stream())
                .mapToInt(chunk -> chunk.contentText().length())
                .sum();
        log.info(
                "[DOCUMENT_PERF] content generation sources prepared. learningNoteId={}, topicCount={}, totalSourceChunks={}, totalSourceCharacters={}, maxChunksPerTopic={}, maxCharactersPerTopic={}",
                learningNoteId,
                sources.size(),
                totalSourceChunks,
                totalSourceCharacters,
                processingProperties.getContentGenerationMaxSourceChunks(),
                processingProperties.getContentGenerationMaxSourceCharacters()
        );

        return sources;
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
        private final int maxSourceChunks;
        private final int maxSourceCharacters;

        private final Map<Long, SourceCandidate> candidates =
                new LinkedHashMap<>();

        private TopicSourceAccumulator(
                ExamScopeNode scopeNode,
                int maxSourceChunks,
                int maxSourceCharacters
        ) {
            this.scopeNode = scopeNode;
            this.maxSourceChunks = Math.max(1, maxSourceChunks);
            this.maxSourceCharacters = Math.max(1, maxSourceCharacters);
        }

        private void addMapping(
                DocumentScopeMapping mapping
        ) {
            DocumentChunk chunk = mapping.getDocumentChunk();

            if (chunk.getId() == null
                    || chunk.getContentText() == null
                    || chunk.getContentText().isBlank()) {

                return;
            }

            SourceCandidate candidate = new SourceCandidate(
                    chunk,
                    mapping.getConfidenceScore(),
                    mapping.getRankNo()
            );
            candidates.merge(
                    chunk.getId(),
                    candidate,
                    (existing, replacement) -> candidateComparator()
                            .compare(existing, replacement) <= 0
                            ? existing
                            : replacement
            );
        }

        private TopicContentSource toSource() {
            List<TopicSourceChunk> selectedChunks = selectSourceChunks();

            return new TopicContentSource(
                    scopeNode.getId(),
                    scopeNode.getCode(),
                    scopeNode.getTitle(),
                    selectedChunks
            );
        }

        private List<TopicSourceChunk> selectSourceChunks() {
            List<SourceCandidate> ranked = candidates.values().stream()
                    .sorted(candidateComparator())
                    .toList();
            List<TopicSourceChunk> selected = new ArrayList<>();
            int selectedCharacters = 0;

            for (SourceCandidate candidate : ranked) {
                if (selected.size() >= maxSourceChunks
                        || selectedCharacters >= maxSourceCharacters) {
                    break;
                }

                DocumentChunk chunk = candidate.chunk();
                int remainingCharacters = maxSourceCharacters - selectedCharacters;
                String contentText = chunk.getContentText();
                if (contentText.length() > remainingCharacters) {
                    contentText = contentText.substring(0, remainingCharacters);
                }
                if (contentText.isBlank()) {
                    continue;
                }

                selected.add(new TopicSourceChunk(
                        chunk.getId(),
                        chunk.getChunkOrder(),
                        chunk.getSectionTitle(),
                        chunk.getContentType().name(),
                        contentText
                ));
                selectedCharacters += contentText.length();
            }

            return selected.stream()
                    .sorted(Comparator.comparingInt(TopicSourceChunk::chunkOrder))
                    .toList();
        }

        private Comparator<SourceCandidate> candidateComparator() {
            return Comparator
                    .comparing(
                            SourceCandidate::confidenceScore,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
                    .thenComparing(
                            SourceCandidate::rankNo,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
                    .thenComparing(candidate -> candidate.chunk().getChunkOrder());
        }

        private record SourceCandidate(
                DocumentChunk chunk,
                java.math.BigDecimal confidenceScore,
                Integer rankNo
        ) {
        }
    }
}
