package com.aha.domain.document.client.mapping;

import com.aha.domain.document.client.mapping.dto.ChunkScopeMappingRequest;
import com.aha.domain.document.client.mapping.dto.ScopeMappingAiResult;

import java.util.List;

public interface DocumentScopeMappingClient {

    List<ScopeMappingAiResult> mapChunks(
            List<ChunkScopeMappingRequest> requests
    );
}