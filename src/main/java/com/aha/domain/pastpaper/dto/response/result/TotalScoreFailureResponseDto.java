package com.aha.domain.pastpaper.dto.response.result;


import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import com.aha.domain.pastpaper.enums.PastPaperFailureReason;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record TotalScoreFailureResponseDto(

    PastPaperFailureReason code,
    String name,
    int userScore,
    int requiredScore
) implements FailureReasonResponseDto {

    public static TotalScoreFailureResponseDto from(TotalResultAggregator totalAggregator) {

        PastPaperFailureReason code = PastPaperFailureReason.TOTAL_SCORE_BELOW_MINIMUM;
        return TotalScoreFailureResponseDto.builder()
            .code(code)
            .name(code.getName())
            .userScore(totalAggregator.getUserScore())
            .requiredScore(totalAggregator.getVersion().getPassingScore())
            .build();
    }
}
