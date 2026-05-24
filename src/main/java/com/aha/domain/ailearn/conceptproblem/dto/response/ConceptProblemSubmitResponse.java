package com.aha.domain.ailearn.conceptproblem.dto.response;

import java.util.List;

public record ConceptProblemSubmitResponse(
        Long learningSessionId,
        Long examScopeNodeId,
        String examScopeNodeTitle,
        int totalCount,
        int correctCount,
        int wrongCount,
        double correctRate,
        List<ConceptProblemResultResponse> results
) {
}