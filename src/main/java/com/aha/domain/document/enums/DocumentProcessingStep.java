package com.aha.domain.document.enums;

public enum DocumentProcessingStep {

    DOCUMENT_PARSING,
    QUALITY_CHECK,
    CHUNKING,
    SCOPE_MAPPING,
    CONTENT_GENERATING,
    FINALIZING,
    COMPLETED;

    public DocumentProcessingStep next() {
        return switch (this) {
            case DOCUMENT_PARSING -> QUALITY_CHECK;
            case QUALITY_CHECK -> CHUNKING;
            case CHUNKING -> SCOPE_MAPPING;
            case SCOPE_MAPPING -> CONTENT_GENERATING;
            case CONTENT_GENERATING -> FINALIZING;
            case FINALIZING -> COMPLETED;
            case COMPLETED -> throw new IllegalStateException(
                    "완료된 단계 이후에는 다음 단계가 없습니다."
            );
        };
    }
}