package com.aha.domain.workbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAnswerRequestDto(
    @Size(max = 500, message = "답안은 500자 이하여야 합니다.")
    @NotBlank
    String userAnswer
) {

}
