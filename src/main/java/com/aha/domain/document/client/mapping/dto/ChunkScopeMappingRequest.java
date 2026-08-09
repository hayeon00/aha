package com.aha.domain.document.client.mapping.dto;

import java.util.List;

public record ChunkScopeMappingRequest(
        ChunkMappingRequest chunk,
        List<ScopeCandidateRequest> scopeCandidates
) {
}
