package com.aha.domain.ailearn.session.dto;

public record LearningSessionCreateRequest(
        Long userId,
        Long examScopeNodeId,
        Long learningContentId
) {
}