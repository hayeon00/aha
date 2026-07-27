package com.aha.domain.notestudio.document.client.mapping;

import com.aha.domain.notestudio.document.dto.mapping.request.ChunkScopeMappingRequestDto;
import com.aha.domain.notestudio.document.dto.mapping.response.ScopeMappingAiResultResponseDto;

import java.util.List;

public interface DocumentScopeMappingClient {

    List<ScopeMappingAiResultResponseDto> mapChunks(
            List<ChunkScopeMappingRequestDto> requests
    );
}