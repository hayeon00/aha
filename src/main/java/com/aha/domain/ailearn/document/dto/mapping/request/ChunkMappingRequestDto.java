package com.aha.domain.ailearn.document.dto.mapping.request;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentChunkMappingRequest
 * @since : 2026. 6. 24. 수요일
 */

public record ChunkMappingRequestDto(
        Long documentChunkId,
        String contentText
) {
}
