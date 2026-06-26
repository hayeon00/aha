package com.aha.domain.ailearn.document.dto.upload.response;

import com.aha.domain.ailearn.document.entity.SourceDocument;

public record UploadedResponseDto(
        Long sourceDocumentId,
        String originalFileName,
        String status
) {

    public static UploadedResponseDto from(
            SourceDocument sourceDocument
    ) {
        return new UploadedResponseDto(
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName(),
                sourceDocument.getUploadStatus().name()
        );
    }
}