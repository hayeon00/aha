package com.aha.domain.document.client.generation.dto;

public record GeneratedTopicContent(
        Long scopeNodeId,
        String title,
        String summary,
        String explanation
) {
}