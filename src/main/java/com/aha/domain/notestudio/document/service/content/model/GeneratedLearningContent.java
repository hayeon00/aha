package com.aha.domain.notestudio.document.service.content.model;

public record GeneratedLearningContent(
        String title,
        String body
) {

    public GeneratedLearningContent {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "생성된 개념설명 제목은 필수입니다."
            );
        }

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException(
                    "생성된 개념설명 내용은 필수입니다."
            );
        }

        title = title.trim();
        body = body.trim();
    }
}