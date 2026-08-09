package com.aha.domain.study.dto.request;

import jakarta.validation.constraints.NotNull;

public record StudyRoomReadyUpdateRequestDto(

    @NotNull(message = "준비 여부는 필수입니다.")
    Boolean ready
) {

}
