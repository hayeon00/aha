package com.aha.domain.ailearn.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentProcessingStep {

    UPLOAD_PENDING("문서 업로드를 준비하고 있어요."),
    UPLOAD_COMPLETED("문서 업로드가 완료되었어요."),
    TEXT_EXTRACTING("문서에서 학습 내용을 읽고 있어요."),
    TEXT_EXTRACTED("문서 내용 추출이 완료되었어요."),
    CONTENT_ANALYZING("문서의 구조와 내용을 분석하고 있어요."),
    CONTENT_ANALYZED("문서 내용 분석이 완료되었어요."),
    SCOPE_MAPPING("문서 내용을 학습 목차와 연결하고 있어요."),
    LEARNING_CONTENT_GENERATING("목차별 개념설명을 생성하고 있어요."),
    LEARNING_CONTENT_GENERATED("개념설명 생성이 완료되었어요.");

    private final String message;
}