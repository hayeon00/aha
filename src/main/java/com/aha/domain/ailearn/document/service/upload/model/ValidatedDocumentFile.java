package com.aha.domain.ailearn.document.service.upload.model;

import org.springframework.web.multipart.MultipartFile;

public record ValidatedDocumentFile(
        MultipartFile file,
        String originalFileName,
        String fileExtension,
        String mimeType,
        long fileSize
) {
}
