package com.aha.domain.notestudio.document.service.content.model;

import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;

public record TopicDocumentChunk(
        Long chunkId,
        Integer chunkOrder,
        DocumentChunkContentType contentType,
        String contentText
) {
    public TopicDocumentChunk {
        if (chunkId == null || contentText == null || contentText.isBlank()) {
            throw new IllegalArgumentException("유효한 문서 청크가 필요합니다.");
        }
        contentType = contentType == null ? DocumentChunkContentType.TEXT : contentType;
        contentText = contentText.trim();
    }
}
