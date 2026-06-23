package com.aha.domain.ailearn.document.dto.upload;

import java.nio.file.Path;

public record StoredDocumentFile(
        String originalFileName,
        String storedFileName,
        String storageKey,
        String fileExtension,
        String mimeType,
        long fileSize,
        Path absolutePath
) {
}