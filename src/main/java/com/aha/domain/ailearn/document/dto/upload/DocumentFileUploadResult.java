package com.aha.domain.ailearn.document.dto.upload;

public record DocumentFileUploadResult(
        Long sourceDocumentId,
        String originalFileName,
        boolean success,
        String errorMessage
) {

    public static DocumentFileUploadResult success(
            Long sourceDocumentId,
            String originalFileName
    ) {
        return new DocumentFileUploadResult(
                sourceDocumentId,
                originalFileName,
                true,
                null
        );
    }

    public static DocumentFileUploadResult failure(
            Long sourceDocumentId,
            String originalFileName,
            String errorMessage
    ) {
        return new DocumentFileUploadResult(
                sourceDocumentId,
                originalFileName,
                false,
                errorMessage
        );
    }
}