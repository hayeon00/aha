package com.aha.domain.pastpaper.dto.request;

import jakarta.validation.constraints.NotNull;

public record AnswerMarkedForReviewRequestDto(

    @NotNull(message = "마크 여부는 필수입니다.")
    Boolean markedForReview
) {

}
