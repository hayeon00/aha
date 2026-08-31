package com.aha.domain.document.service.processing.parsing.model;

import java.util.List;

public record ParsedDocument(
        List<DocumentBlock> blocks
) {

    public ParsedDocument {
        blocks = blocks == null
                ? List.of()
                : List.copyOf(blocks);
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public int totalTextLength() {
        return blocks.stream()
                .filter(block ->
                        block != null
                                && block.text() != null
                )
                .mapToInt(block ->
                        block.text().length()
                )
                .sum();
    }
}