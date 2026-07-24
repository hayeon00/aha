package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.pastpaper.entity.ProblemChoice;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ProblemChoiceResponseDto(

    int sortOrder,
    String content
) {

    public static ProblemChoiceResponseDto from(ProblemChoice choice){
        return ProblemChoiceResponseDto.builder()
            .sortOrder(choice.getSortOrder())
            .content(choice.getContent())
            .build();
    }
}
