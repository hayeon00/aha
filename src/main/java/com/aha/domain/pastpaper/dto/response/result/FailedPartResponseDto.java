package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.pastpaper.aggregation.PartResultAggregator;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record FailedPartResponseDto(
    String name,
    int userScore,
    int requiredScore
) {


    public static FailedPartResponseDto from(PartResultAggregator partAggregator) {
        ExamPart part = partAggregator.getPart();
        return FailedPartResponseDto.builder()
            .name(part.getName())
            .userScore(partAggregator.getUserScore())
            .requiredScore(part.getSubjectFailThresholdScore())
            .build();
    }
}
