package com.aha.domain.document.repository.projection;

import com.aha.domain.document.enums.DocumentChunkContentType;

public interface MappedTopicChunkProjection {

    Long getTopicId();

    String getTopicTitle();

    Long getChunkId();

    Integer getChunkOrder();

    String getChunkText();

    DocumentChunkContentType getChunkType();
}
