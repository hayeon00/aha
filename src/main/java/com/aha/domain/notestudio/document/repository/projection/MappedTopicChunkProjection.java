package com.aha.domain.notestudio.document.repository.projection;

import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;

public interface MappedTopicChunkProjection {

    Long getTopicId();

    String getTopicTitle();

    Long getChunkId();

    Integer getChunkOrder();

    String getChunkText();

    DocumentChunkContentType getChunkType();
}
