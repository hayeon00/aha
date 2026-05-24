package com.aha.domain.ailearn.conceptproblem.dto.response;

public record ConceptProblemResultResponse(
        Long problemId,
        String questionText,
        Integer selectedChoiceNo,
        Integer correctChoiceNo,
        boolean correct,
        String explanationText
) {
}