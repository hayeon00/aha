package com.aha.domain.ailearn.document.dto.mapping.request;

public record ExamScopeCandidateDto(
        Long examScopeNodeId,
        String title,
        String path
) {
}