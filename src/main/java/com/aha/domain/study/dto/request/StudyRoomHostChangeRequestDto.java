package com.aha.domain.study.dto.request;

import jakarta.validation.constraints.Positive;

public record StudyRoomHostChangeRequestDto(

    @Positive(message = "멤버 ID는 양수여야 합니다.")
    long studyRoomMemberId
) {

}
