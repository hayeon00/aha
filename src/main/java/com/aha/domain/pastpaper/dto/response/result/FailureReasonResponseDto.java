package com.aha.domain.pastpaper.dto.response.result;


import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import com.aha.domain.pastpaper.enums.PastPaperFailureReason;

public sealed interface FailureReasonResponseDto
    permits TotalScoreFailureResponseDto, PartScoreFailureResponseDto {


    static FailureReasonResponseDto of(PastPaperFailureReason reason,
        TotalResultAggregator totalAggregator) {

        return switch (reason) {
            case TOTAL_SCORE_BELOW_MINIMUM ->
                TotalScoreFailureResponseDto.from(
                    totalAggregator
                );

            case SUBJECT_SCORE_BELOW_MINIMUM ->
                PartScoreFailureResponseDto.from(
                    totalAggregator.getFailedPartAggregators()
                );
        };
    }
}
