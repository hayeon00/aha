package com.aha.domain.notestudio.document.dto.mapping.request;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentChunkMappingRequest
 * @since : 2026. 6. 24. 수요일
 */

public record ChunkMappingRequestDto(
        Long documentChunkId,
        Integer chunkOrder,
        String sectionTitle,
        String headingPath,
        String contentType,
        String codeLanguage,
        String contentText
) {
}
