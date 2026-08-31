package com.aha.domain.document.service.processing.generation.model;

import java.util.List;

public record TopicContentSource(
        Long scopeNodeId,
        String scopeCode,
        String scopeTitle,
        List<TopicSourceChunk> chunks
) {

    public TopicContentSource {

        chunks = chunks == null
                ? List.of()
                : List.copyOf(chunks);
    }
}