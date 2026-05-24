package com.aha.domain.ailearn.progress.dto.response;

public record LearningProgressSummaryResponse(
        Long examVersionId,
        long totalTopicCount,
        long completedTopicCount,
        double progressRate
) {
}