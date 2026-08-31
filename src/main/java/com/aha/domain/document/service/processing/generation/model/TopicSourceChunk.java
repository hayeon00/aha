package com.aha.domain.document.service.processing.generation.model;

public record TopicSourceChunk(
        Long documentChunkId,
        Integer chunkOrder,
        String sectionTitle,
        String contentType,
        String contentText
) {
}
