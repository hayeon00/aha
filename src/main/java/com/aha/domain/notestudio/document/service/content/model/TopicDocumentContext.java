package com.aha.domain.notestudio.document.service.content.model;

import java.util.List;

public record TopicDocumentContext(
        Long topicId,
        String topicTitle,
        List<TopicDocumentChunk> chunks
) {

    public TopicDocumentContext {
        if (topicId == null) {
            throw new IllegalArgumentException(
                    "Topic ID는 필수입니다."
            );
        }

        if (topicTitle == null || topicTitle.isBlank()) {
            throw new IllegalArgumentException(
                    "Topic 제목은 필수입니다."
            );
        }

        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "개념설명 생성에 사용할 문서 청크는 필수입니다."
            );
        }

        chunks = chunks.stream()
                .filter(java.util.Objects::nonNull)
                .toList();

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "유효한 문서 청크가 없습니다."
            );
        }
    }

    public String combinedText() {
        return String.join(
                "\n\n",
                chunks.stream().map(TopicDocumentChunk::contentText).toList()
        );
    }
}
