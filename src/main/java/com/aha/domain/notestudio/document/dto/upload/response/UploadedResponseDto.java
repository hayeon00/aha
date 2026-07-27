package com.aha.domain.notestudio.document.dto.upload.response;

import com.aha.domain.notestudio.document.entity.SourceDocument;

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