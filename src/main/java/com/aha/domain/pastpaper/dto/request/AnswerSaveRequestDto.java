package com.aha.domain.pastpaper.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AnswerSaveRequestDto(

    @NotBlank
    String userAnswer
) {

}
