package com.aha.domain.concept.dto.response;

import com.aha.domain.concept.entity.Concept;

public record ConceptListItemResponse(
        Long conceptId,
        String code,
        String title,
        String summary,
        int displayOrder,
        Long scopeNodeId,
        String scopeTitle
) {
    public static ConceptListItemResponse from(Concept concept) {
        return new ConceptListItemResponse(
                concept.getId(),
                concept.getCode(),
                concept.getTitle(),
                concept.getSummary(),
                concept.getDisplayOrder(),
                concept.getExamScopeNode().getId(),
                concept.getExamScopeNode().getTitle()
        );
    }
}
