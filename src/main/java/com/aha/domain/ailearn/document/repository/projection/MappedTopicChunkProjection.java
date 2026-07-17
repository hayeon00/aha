package com.aha.domain.ailearn.document.repository.projection;

import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;

public interface MappedTopicChunkProjection {

    Long getTopicId();

    String getTopicTitle();

    Long getChunkId();

    Integer getChunkOrder();

    String getChunkText();

    DocumentChunkContentType getChunkType();
}
