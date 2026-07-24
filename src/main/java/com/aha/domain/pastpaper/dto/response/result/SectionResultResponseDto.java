package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.pastpaper.aggregation.SectionResultAggregator;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)

public record SectionResultResponseDto(

    String code,
    String name,
    int userScore,
    int maxScore
) {

    public static SectionResultResponseDto from(SectionResultAggregator sectionResultAggregator) {

        ExamScopeNode node =  sectionResultAggregator.getNode();
        return SectionResultResponseDto.builder()
            .code(node.getCode())
            .name(node.getTitle())
            .userScore(sectionResultAggregator.getUserScore())
            .maxScore(sectionResultAggregator.getMaxScore())
            .build();
    }
}
