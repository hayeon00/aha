package com.aha.domain.ailearn.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentProcessingStep {

    FILE_UPLOADED(10),
    TEXT_EXTRACTING(35),
    SCOPE_MAPPING(65),
    LEARNING_CONTENT_GENERATING(85),
    COMPLETED(100);

    private final int progressRate;
}