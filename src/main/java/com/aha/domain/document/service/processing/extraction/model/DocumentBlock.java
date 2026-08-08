package com.aha.domain.document.service.processing.extraction.model;

import com.aha.domain.document.enums.DocumentChunkCodeLanguage;
import com.aha.domain.document.enums.DocumentChunkContentType;

public record DocumentBlock(
        Integer pageNo,
        String headingPath,
        String sectionTitle,
        DocumentChunkContentType contentType,
        DocumentChunkCodeLanguage codeLanguage,
        String text,
        String rawText
) {
    public boolean isBlank() {
        return text == null || text.isBlank();
    }

    public boolean isCodeBlock() {
        return contentType == DocumentChunkContentType.CODE;
    }

    public DocumentChunkCodeLanguage resolvedCodeLanguage() {
        if (contentType != DocumentChunkContentType.CODE
                && contentType != DocumentChunkContentType.COMMAND
                && contentType != DocumentChunkContentType.CONFIG) {
            return null;
        }

        return codeLanguage == null
                ? DocumentChunkCodeLanguage.UNKNOWN
                : codeLanguage;
    }
}
