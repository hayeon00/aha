package com.aha.domain.learningnote.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LearningNoteTitleUpdateRequestDto(
        @NotBlank
        @Size(max = 255)
        String title
) {
}
