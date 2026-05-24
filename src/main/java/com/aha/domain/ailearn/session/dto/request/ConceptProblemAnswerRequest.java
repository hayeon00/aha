package com.aha.domain.ailearn.session.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConceptProblemAnswerRequest(

        @NotNull(message = "문제 ID는 필수입니다.")
        Long problemId,

        @NotNull(message = "선택한 보기 번호는 필수입니다.")
        Integer selectedChoiceNo

) {
}