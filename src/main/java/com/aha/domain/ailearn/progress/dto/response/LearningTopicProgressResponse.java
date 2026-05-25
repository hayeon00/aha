package com.aha.domain.ailearn.progress.dto.response;

public record LearningTopicProgressResponse(
        Long examScopeNodeId,
        String status
) {
}