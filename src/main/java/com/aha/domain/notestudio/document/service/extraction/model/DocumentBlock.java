package com.aha.domain.notestudio.document.service.extraction.model;

import com.aha.domain.notestudio.document.enums.DocumentChunkCodeLanguage;
import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentBlock
 * @since : 2026. 7. 8. 수요일
 */

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