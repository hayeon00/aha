package com.aha.domain.ailearn.document.dto.generation.request;

import java.util.List;

public record TopicLearningContentSourceDto(
        Long examScopeNodeId,
        String scopeTitle,
        String scopePath,
        List<String> chunkContents
) {
}

