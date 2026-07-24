package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperAttemptResultResponseDto(

    int maxScore,
    int userScore,
    boolean passed,
    List<FailureReasonResponseDto> failureReasons,
    List<PartResultResponseDto> partResults
) {


    public static PastPaperAttemptResultResponseDto from(TotalResultAggregator totalAggregator) {

        List<FailureReasonResponseDto> failureReasons = totalAggregator.getFailureReasons().stream()
            .map(reason-> FailureReasonResponseDto.of(reason,totalAggregator)).toList();

        List<PartResultResponseDto> partResults = totalAggregator.getPartAggregators().stream()
            .map(PartResultResponseDto::from).toList();

        return PastPaperAttemptResultResponseDto.builder()
            .maxScore(totalAggregator.getVersion().getTotalScore())
            .userScore(totalAggregator.getUserScore())
            .passed(totalAggregator.isPassed())
            .failureReasons(failureReasons)
            .partResults(partResults)
            .build();
    }
}
