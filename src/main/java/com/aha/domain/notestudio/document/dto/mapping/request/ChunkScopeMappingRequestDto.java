package com.aha.domain.notestudio.document.dto.mapping.request;

import java.util.List;

public record ChunkScopeMappingRequestDto(
        ChunkMappingRequestDto chunk,
        List<ScopeCandidateRequestDto> scopeCandidates
) {
}