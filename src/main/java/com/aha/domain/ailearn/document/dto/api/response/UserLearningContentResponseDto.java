package com.aha.domain.ailearn.document.dto.api.response;

import java.util.List;

public record UserLearningContentResponseDto(
        Long id,
        Long userExamId,
        Long examScopeNodeId,
        String title,
        String content,
        List<String> keywords,
        String status
) {
}