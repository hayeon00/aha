package com.aha.domain.ailearn.conceptproblem.dto.response;

import java.util.List;

public record ConceptProblemListResponse(
        Long learningSessionId,
        Long examScopeNodeId,
        String examScopeNodeTitle,
        int totalCount,
        List<ConceptProblemResponse> problems
) {
}