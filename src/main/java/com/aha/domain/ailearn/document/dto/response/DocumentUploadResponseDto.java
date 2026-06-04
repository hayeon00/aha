package com.aha.domain.ailearn.document.dto.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.SourceDocument;

public record DocumentUploadResponseDto(
        Long sourceDocumentId,
        Long processingId,
        Long userExamId,
        String originalFileName,
        String fileExtension,
        Long fileSize,
        String documentStatus,
        String processingStatus,
        String currentStep,
        Integer progressRate
) {

    public static DocumentUploadResponseDto of(
            SourceDocument sourceDocument,
            DocumentProcessing documentProcessing
    ) {
        return new DocumentUploadResponseDto(
                sourceDocument.getId(),
                documentProcessing.getId(),
                sourceDocument.getUserExam().getId(),
                sourceDocument.getOriginalFileName(),
                sourceDocument.getFileExtension(),
                sourceDocument.getFileSize(),
                sourceDocument.getStatus(),
                documentProcessing.getStatus(),
                documentProcessing.getCurrentStep(),
                documentProcessing.getProgressRate()
        );
    }
}