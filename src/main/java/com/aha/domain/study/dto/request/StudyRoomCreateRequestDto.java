package com.aha.domain.study.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyRoomCreateRequestDto(

    @NotBlank
    @Size(min = 1, max = 100, message = "제목은 최소 1글자 이상 최대 100글자를 입력해야합니다.")
    String title,

    @NotBlank
    @Size(min = 1, max = 500, message = "설명은 최소 1글자 이상 최대 500글자를 입력해야합니다.")
    String description,

    @Min(1)
    long pastPaperId,

    @Max(value = 5,message = "정원은 5명이 최대입니다.")
    @Min(value = 2,message = "정원은 2명 이상이어야합니다.")
    int capacity,

    @Min(value = 1,message = "시간 제한은 1초 이상이어야합니다")
    int timeLimit
) {

}
