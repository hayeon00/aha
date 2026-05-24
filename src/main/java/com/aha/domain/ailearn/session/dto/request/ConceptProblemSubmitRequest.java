package com.aha.domain.ailearn.session.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConceptProblemSubmitRequest(

        @NotEmpty(message = "제출할 답안이 없습니다.")
        @Valid
        List<ConceptProblemAnswerRequest> answers

) {
}