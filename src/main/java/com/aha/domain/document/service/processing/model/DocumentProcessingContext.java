package com.aha.domain.document.service.processing.model;

public record DocumentProcessingContext(
        Long processingId,
        Long learningNoteId,
        Long sourceDocumentId
) {
}
