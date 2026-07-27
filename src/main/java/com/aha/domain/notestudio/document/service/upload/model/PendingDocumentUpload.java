package com.aha.domain.notestudio.document.service.upload.model;

public record PendingDocumentUpload(
        Long sourceDocumentId,
        ValidatedDocumentFile validatedFile,
        String storageKey
) {

    public PendingDocumentUpload {
        if (sourceDocumentId == null) {
            throw new IllegalArgumentException(
                    "SourceDocument ID는 필수입니다."
            );
        }

        if (validatedFile == null) {
            throw new IllegalArgumentException(
                    "검증된 문서 파일은 필수입니다."
            );
        }

        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 저장 키는 필수입니다."
            );
        }
    }
}