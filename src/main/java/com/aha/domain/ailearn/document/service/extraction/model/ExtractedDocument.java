package com.aha.domain.ailearn.document.service.extraction.model;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ExtractedDocument
 * @since : 2026. 7. 8. 수요일
 */

public record ExtractedDocument(
        String fullText,
        List<DocumentBlock> blocks
) {

    public static ExtractedDocument of(String fullText, List<DocumentBlock> blocks) {
        return new ExtractedDocument(
                fullText,
                blocks == null ? List.of() : List.copyOf(blocks)
        );
    }

    public boolean isEmpty() {
        return (fullText == null || fullText.isBlank())
                && (blocks == null || blocks.isEmpty());
    }
}
