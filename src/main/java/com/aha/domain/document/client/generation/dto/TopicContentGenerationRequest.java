package com.aha.domain.document.client.generation.dto;

import java.util.List;

public record TopicContentGenerationRequest(
        Long scopeNodeId,
        String scopeTitle,
        List<SourceChunkRequest> sourceChunks
) {

    public record SourceChunkRequest(
            Long documentChunkId,
            String sectionTitle,
            String contentText
    ) {
    }
}