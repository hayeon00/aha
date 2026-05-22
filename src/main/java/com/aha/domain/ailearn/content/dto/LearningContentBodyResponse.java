package com.aha.domain.ailearn.content.dto;

import com.aha.domain.ailearn.content.entity.ContentBodyType;
import com.aha.domain.ailearn.content.entity.LearningContentBody;

public record LearningContentBodyResponse(
        Long id,
        ContentBodyType bodyType,
        String title,
        String bodyText,
        Integer displayOrder,
        Integer ragChunkOrder
) {

    public static LearningContentBodyResponse from(LearningContentBody body) {
        return new LearningContentBodyResponse(
                body.getId(),
                body.getBodyType(),
                body.getTitle(),
                body.getBodyText(),
                body.getDisplayOrder(),
                body.getRagChunkOrder()
        );
    }
}