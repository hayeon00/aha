package com.aha.domain.ailearn.document.client.mapping;

import com.aha.domain.ailearn.document.dto.mapping.request.ChunkMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ScopeCandidateRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResultResponseDto;

import java.util.List;

public interface DocumentScopeMappingClient {

    List<ScopeMappingAiResultResponseDto> mapChunks(
            List<ChunkMappingRequestDto> chunks,
            List<ScopeCandidateRequestDto> scopeCandidates
    );
}