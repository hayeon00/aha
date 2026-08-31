package com.aha.domain.document.model;

import com.aha.domain.document.enums.DocumentFileExtension;
import org.springframework.web.multipart.MultipartFile;

public record ValidatedDocumentFile(

        MultipartFile file,
        String originalFileName,
        DocumentFileExtension fileExtension,
        String mimeType,
        long fileSize

) {
}