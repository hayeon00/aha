package com.aha.domain.notestudio.document.service.extraction.model;

import com.aha.domain.notestudio.document.entity.SourceDocument;

public record ExtractedDocumentContext(
        SourceDocument sourceDocument,
        ExtractedDocument extractedDocument
) {

    public ExtractedDocumentContext {
        if (sourceDocument == null) {
            throw new IllegalArgumentException(
                    "원본 문서는 필수입니다."
            );
        }

        if (extractedDocument == null) {
            throw new IllegalArgumentException(
                    "문서 추출 결과는 필수입니다."
            );
        }
    }
}