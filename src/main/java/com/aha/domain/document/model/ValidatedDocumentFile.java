package com.aha.domain.document.model;

import org.springframework.web.multipart.MultipartFile;

public record ValidatedDocumentFile(
        MultipartFile file,
        String originalFileName,
        String fileExtension,
        String mimeType,
        long fileSize
) {
}
