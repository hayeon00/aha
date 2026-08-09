package com.aha.domain.document.service.processing.extraction.model;

import java.util.List;

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
