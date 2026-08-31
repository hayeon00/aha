package com.aha.domain.document.service.processing.mapping;

import com.aha.domain.document.config.DocumentProcessingProperties;
import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.enums.DocumentScopeMappingMethod;
import com.aha.domain.document.service.processing.embedding.similarity.model.ScopeCandidateSearchResult;
import com.aha.domain.exam.entity.ExamScopeNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SemanticScopeMappingFastPathTest {

    @Test
    void mapsOnlyHighSimilarityCandidateWithSafeMargin() {
        DocumentProcessingProperties properties = new DocumentProcessingProperties();
        properties.setSemanticFastPathEnabled(true);
        properties.setSemanticFastPathMinSimilarity(0.92);
        properties.setSemanticFastPathMinMargin(0.20);
        SemanticScopeMappingFastPath fastPath =
                new SemanticScopeMappingFastPath(properties);

        DocumentChunk confidentChunk = chunk(1L);
        DocumentChunk ambiguousChunk = chunk(2L);
        ExamScopeNode firstScope = scopeNode(10L);
        ExamScopeNode secondScope = scopeNode(20L);

        Map<Long, List<ScopeCandidateSearchResult>> candidates = Map.of(
                1L, List.of(
                        candidate(1L, firstScope, 0.95, 1),
                        candidate(1L, secondScope, 0.70, 2)
                ),
                2L, List.of(
                        candidate(2L, firstScope, 0.88, 1),
                        candidate(2L, secondScope, 0.80, 2)
                )
        );

        SemanticScopeMappingFastPath.Result result = fastPath.evaluate(
                List.of(confidentChunk, ambiguousChunk),
                candidates
        );

        assertThat(result.mappedChunkIds()).containsExactly(1L);
        assertThat(result.mappings()).hasSize(1);
        assertThat(result.mappings().get(0).getMappingMethod())
                .isEqualTo(DocumentScopeMappingMethod.SEMANTIC_SIMILARITY);
    }

    private DocumentChunk chunk(Long id) {
        DocumentChunk chunk = mock(DocumentChunk.class);
        when(chunk.getId()).thenReturn(id);
        when(chunk.getSectionTitle()).thenReturn("Topic " + id);
        return chunk;
    }

    @Test
    void skipsChunkWithoutSectionTitleEvenWithHighSimilarity() {
        DocumentProcessingProperties properties = new DocumentProcessingProperties();
        properties.setSemanticFastPathEnabled(true);
        properties.setSemanticFastPathMinSimilarity(0.92);
        properties.setSemanticFastPathMinMargin(0.20);
        SemanticScopeMappingFastPath fastPath =
                new SemanticScopeMappingFastPath(properties);

        DocumentChunk chunk = mock(DocumentChunk.class);
        when(chunk.getId()).thenReturn(1L);
        ExamScopeNode firstScope = scopeNode(10L);
        ExamScopeNode secondScope = scopeNode(20L);

        SemanticScopeMappingFastPath.Result result = fastPath.evaluate(
                List.of(chunk),
                Map.of(1L, List.of(
                        candidate(1L, firstScope, 0.99, 1),
                        candidate(1L, secondScope, 0.50, 2)
                ))
        );

        assertThat(result.mappedChunkIds()).isEmpty();
        assertThat(result.mappings()).isEmpty();
    }

    private ExamScopeNode scopeNode(Long id) {
        ExamScopeNode scopeNode = mock(ExamScopeNode.class);
        when(scopeNode.getId()).thenReturn(id);
        return scopeNode;
    }

    private ScopeCandidateSearchResult candidate(
            Long chunkId,
            ExamScopeNode scopeNode,
            double similarity,
            int rank
    ) {
        return new ScopeCandidateSearchResult(
                chunkId,
                scopeNode,
                similarity,
                rank
        );
    }
}
