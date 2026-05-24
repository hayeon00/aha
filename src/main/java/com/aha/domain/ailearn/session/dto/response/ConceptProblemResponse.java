package com.aha.domain.ailearn.session.dto.response;

import java.util.List;

public record ConceptProblemResponse(
        Long problemId,
        String questionText,
        List<ConceptProblemChoiceResponse> choices
) {
}