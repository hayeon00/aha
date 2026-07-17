package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import java.math.BigDecimal;

public record MappedDocumentChunkResponseDto(
        Long documentChunkId,
        Integer chunkOrder,
        Integer pageNo,
        String sectionTitle,
        String headingPath,
        String contentType,
        String codeLanguage,
        String contentText,
        BigDecimal mappingConfidence,
        String mappingStatus
) {

    public static MappedDocumentChunkResponseDto from(DocumentChunk documentChunk) {
        return new MappedDocumentChunkResponseDto(
                documentChunk.getId(),
                documentChunk.getChunkOrder(),
                documentChunk.getPageNo(),
                documentChunk.getSectionTitle(),
                documentChunk.getHeadingPath(),
                documentChunk.getContentType() == null ? null : documentChunk.getContentType().name(),
                documentChunk.getCodeLanguage() == null ? null : documentChunk.getCodeLanguage().name(),
                documentChunk.getContentText(),
                documentChunk.getMappingConfidence(),
                documentChunk.getMappingStatus().name()
        );
    }

    public static MappedDocumentChunkResponseDto from(DocumentScopeMapping mapping) {
        DocumentChunk chunk = mapping.getDocumentChunk();
        return new MappedDocumentChunkResponseDto(
                chunk.getId(),
                chunk.getChunkOrder(),
                chunk.getPageNo(),
                chunk.getSectionTitle(),
                chunk.getHeadingPath(),
                chunk.getContentType() == null ? null : chunk.getContentType().name(),
                chunk.getCodeLanguage() == null ? null : chunk.getCodeLanguage().name(),
                chunk.getContentText(),
                mapping.getConfidenceScore(),
                chunk.getMappingStatus().name()
        );
    }
}
