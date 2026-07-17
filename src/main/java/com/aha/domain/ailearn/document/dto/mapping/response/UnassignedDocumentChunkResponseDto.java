package com.aha.domain.ailearn.document.dto.mapping.response;

import com.aha.domain.ailearn.document.entity.DocumentChunk;

import java.math.BigDecimal;

public record UnassignedDocumentChunkResponseDto(
        Long documentChunkId,
        String fileName,
        Integer pageNo,
        String sectionTitle,
        String contentText,
        BigDecimal bestSimilarityScore,
        String mappingStatus
) {
    public static UnassignedDocumentChunkResponseDto from(DocumentChunk chunk) {
        return new UnassignedDocumentChunkResponseDto(
                chunk.getId(),
                chunk.getSourceDocument().getOriginalFileName(),
                chunk.getPageNo(),
                chunk.getSectionTitle(),
                chunk.getContentText(),
                chunk.getMappingConfidence(),
                chunk.getMappingStatus().name()
        );
    }
}
