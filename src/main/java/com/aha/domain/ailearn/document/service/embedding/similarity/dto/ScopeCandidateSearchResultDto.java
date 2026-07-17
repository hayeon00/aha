package com.aha.domain.ailearn.document.service.embedding.similarity.dto;

import com.aha.domain.exam.entity.ExamScopeNode;

public record ScopeCandidateSearchResultDto(
        Long documentChunkId,
        ExamScopeNode examScopeNode,
        double similarityScore,
        int rankNo
) {
}