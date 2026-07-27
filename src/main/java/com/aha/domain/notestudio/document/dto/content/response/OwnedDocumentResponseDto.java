package com.aha.domain.notestudio.document.dto.content.response;

import com.aha.domain.notestudio.document.entity.SourceDocument;

public record OwnedDocumentResponseDto(Long documentId, String fileName) {
    public static OwnedDocumentResponseDto from(SourceDocument document) {
        return new OwnedDocumentResponseDto(document.getId(), document.getOriginalFileName());
    }
}
