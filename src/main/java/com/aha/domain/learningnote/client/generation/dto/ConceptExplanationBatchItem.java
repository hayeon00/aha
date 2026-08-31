package com.aha.domain.learningnote.client.generation.dto;

public record ConceptExplanationBatchItem(
        Long examScopeNodeId,
        String title,
        String content
) {
}
