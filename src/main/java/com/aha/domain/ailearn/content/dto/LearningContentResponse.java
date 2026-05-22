package com.aha.domain.ailearn.content.dto;

import com.aha.domain.ailearn.content.entity.LearningContent;

import java.util.List;

public record LearningContentResponse(
        Long id,
        Long examScopeNodeId,
        String title,
        String summary,
        Boolean ragEnabled,
        Integer displayOrder,
        List<LearningContentBodyResponse> bodies
) {

    public static LearningContentResponse of(
            LearningContent content,
            List<LearningContentBodyResponse> bodies
    ) {
        return new LearningContentResponse(
                content.getId(),
                content.getExamScopeNodeId(),
                content.getTitle(),
                content.getSummary(),
                content.getRagEnabled(),
                content.getDisplayOrder(),
                bodies
        );
    }
}