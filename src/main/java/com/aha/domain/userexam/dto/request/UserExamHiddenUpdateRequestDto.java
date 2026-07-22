package com.aha.domain.userexam.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserExamHiddenUpdateRequestDto(
        @NotNull(message = "hidden 값은 필수입니다.")
        Boolean hidden
) {
}