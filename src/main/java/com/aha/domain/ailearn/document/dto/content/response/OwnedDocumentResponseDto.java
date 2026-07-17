package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.SourceDocument;

public record OwnedDocumentResponseDto(Long documentId, String fileName) {
    public static OwnedDocumentResponseDto from(SourceDocument document) {
        return new OwnedDocumentResponseDto(document.getId(), document.getOriginalFileName());
    }
}
