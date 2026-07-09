package com.aha.domain.ailearn.document.service.extraction.model;

import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;

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
        String text,
        String rawText
) {
    public boolean isBlank() {
        return text == null || text.isBlank();
    }
}
