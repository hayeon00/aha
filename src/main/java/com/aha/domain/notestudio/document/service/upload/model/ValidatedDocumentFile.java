package com.aha.domain.notestudio.document.service.upload.model;

import org.springframework.web.multipart.MultipartFile;

public record ValidatedDocumentFile(
        MultipartFile file,
        String originalFileName,
        String fileExtension,
        String mimeType,
        long fileSize
) {
}
