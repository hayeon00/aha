package com.aha.domain.userexam.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ExamOnboardingRequestDto(
        @NotEmpty(message = "하나 이상의 시험을 선택해주세요.")
        Set<Long> examIds
) {
}
