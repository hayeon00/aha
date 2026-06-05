package com.aha.domain.ailearn.document.enums;

public enum DocumentProcessingStep {
    FILE_UPLOADED(10),
    TEXT_EXTRACTING(25),
    TEXT_EXTRACTED(25),
    CONTENT_ANALYZING(45),
    CONTENT_ANALYZED(45),
    SCOPE_MAPPING(70),
    SCOPE_MAPPED(70),
    LEARNING_CONTENT_GENERATING(85),
    COMPLETED(100),
    FAILED(0);

    private final int progressRate;

    DocumentProcessingStep(int progressRate) {
        this.progressRate = progressRate;
    }

    public int getProgressRate() {
        return progressRate;
    }
}
