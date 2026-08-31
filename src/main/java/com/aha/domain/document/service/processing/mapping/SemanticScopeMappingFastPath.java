package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.config.DocumentProcessingProperties;
import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.enums.DocumentScopeMappingMethod;
import com.aha.domain.document.service.processing.embedding.similarity.model.ScopeCandidateSearchResult;
import com.aha.domain.exam.entity.ExamScopeNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SemanticScopeMappingFastPath {

    private final DocumentProcessingProperties properties;

    public Result evaluate(
            List<DocumentChunk> chunks,
            Map<Long, List<ScopeCandidateSearchResult>> candidatesByChunkId
    ) {
        if (!properties.isSemanticFastPathEnabled()) {
            return new Result(List.of(), Set.of());
        }

        List<DocumentScopeMapping> mappings = new ArrayList<>();
        Set<Long> mappedChunkIds = new HashSet<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk == null || chunk.getId() == null) {
                continue;
            }
            if (chunk.getSectionTitle() == null
                    || chunk.getSectionTitle().isBlank()) {
                continue;
            }

            List<ScopeCandidateSearchResult> candidates = candidatesByChunkId
                    .getOrDefault(chunk.getId(), List.of())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(candidate -> candidate.examScopeNode() != null)
                    .sorted(Comparator.comparingDouble(
                            ScopeCandidateSearchResult::similarityScore
                    ).reversed())
                    .toList();

            if (candidates.isEmpty()) {
                continue;
            }

            ScopeCandidateSearchResult best = candidates.get(0);
            double secondScore = candidates.size() > 1
                    ? candidates.get(1).similarityScore()
                    : 0.0;
            double margin = best.similarityScore() - secondScore;

            if (best.similarityScore() < properties.getSemanticFastPathMinSimilarity()
                    || margin < properties.getSemanticFastPathMinMargin()) {
                continue;
            }

            ExamScopeNode scopeNode = best.examScopeNode();
            mappings.add(DocumentScopeMapping.create(
                    chunk,
                    scopeNode,
                    1,
                    BigDecimal.valueOf(best.similarityScore())
                            .setScale(4, RoundingMode.HALF_UP),
                    DocumentScopeMappingMethod.SEMANTIC_SIMILARITY,
                    "임베딩 Top-1 유사도와 후보 간 점수 차이가 안전 기준을 충족함"
            ));
            mappedChunkIds.add(chunk.getId());
        }

        return new Result(List.copyOf(mappings), Set.copyOf(mappedChunkIds));
    }

    public record Result(
            List<DocumentScopeMapping> mappings,
            Set<Long> mappedChunkIds
    ) {
    }
}
