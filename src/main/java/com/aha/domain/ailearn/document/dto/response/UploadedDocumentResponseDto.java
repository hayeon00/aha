package com.aha.domain.ailearn.document.dto.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.SourceDocument;

public record UploadedDocumentResponseDto(
        Long sourceDocumentId,
        Long processingId,
        String originalFileName,
        String fileExtension,
        Long fileSize,
        String documentStatus,
        String processingStatus
) {

    public static UploadedDocumentResponseDto of(
            SourceDocument sourceDocument,
            DocumentProcessing documentProcessing
    ) {
        return new UploadedDocumentResponseDto(
                sourceDocument.getId(),
                documentProcessing.getId(),
                sourceDocument.getOriginalFileName(),
                sourceDocument.getFileExtension(),
                sourceDocument.getFileSize(),
                sourceDocument.getStatus().name(),
                documentProcessing.getStatus().name()
        );
    }
}