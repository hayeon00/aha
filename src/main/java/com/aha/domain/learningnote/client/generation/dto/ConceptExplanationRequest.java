package com.aha.domain.learningnote.client.generation.dto;

import java.util.List;

public record ConceptExplanationRequest(
        Long examScopeNodeId,
        String topicTitle,
        List<ConceptSourceChunk> sourceChunks
) {
}
