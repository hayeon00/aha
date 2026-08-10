package com.aha.domain.document.service.processing.embedding.similarity.model;

import com.aha.domain.exam.entity.ExamScopeNode;

public record ScopeCandidateSearchResult(
        Long documentChunkId,
        ExamScopeNode examScopeNode,
        double similarityScore,
        int rankNo
) {
}
