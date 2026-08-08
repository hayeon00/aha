package com.aha.domain.document.model;

public record StoredDocumentFile(
        String storedFileName,
        String storageKey
) {
}