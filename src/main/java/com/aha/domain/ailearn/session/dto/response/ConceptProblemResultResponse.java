package com.aha.domain.ailearn.session.dto.response;

public record ConceptProblemResultResponse(
        Long problemId,
        String questionText,
        Integer selectedChoiceNo,
        Integer correctChoiceNo,
        boolean correct,
        String explanationText
) {
}