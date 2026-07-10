package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.DocumentChunk;

public record MappedDocumentChunkResponseDto(
        Long documentChunkId,
        Integer chunkOrder,
        Integer pageNo,
        String sectionTitle,
        String headingPath,
        String contentType,
        String codeLanguage,
        String contentText
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
                documentChunk.getContentText()
        );
    }
}