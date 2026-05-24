package com.aha.domain.ailearn.progress.dto.response;

import java.util.List;

public record LearningProgressSummaryResponse(
        Long examVersionId,
        long totalTopicCount,
        long completedTopicCount,
        double progressRate,
        List<LearningTopicProgressResponse> topics
) {
}