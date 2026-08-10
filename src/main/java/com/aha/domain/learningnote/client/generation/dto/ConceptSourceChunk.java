package com.aha.domain.learningnote.client.generation.dto;

public record ConceptSourceChunk(
        Long chunkId,
        Integer chunkOrder,
        String contentType,
        String content
) {
}
