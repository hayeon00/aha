package com.aha.domain.ailearn.conceptproblem.dto.response;

import java.util.List;

public record ConceptProblemResponse(
        Long problemId,
        String questionText,
        List<ConceptProblemChoiceResponse> choices
) {
}