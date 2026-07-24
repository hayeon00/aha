package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperAttemptSubmitResponseDto(

    PastPaperAttemptStatus attemptStatus,
    PastPaperAttemptResultResponseDto result
) {

    public static PastPaperAttemptSubmitResponseDto of(PastPaperAttempt attempt,TotalResultAggregator totalAggregator) {
        return PastPaperAttemptSubmitResponseDto.builder()
            .attemptStatus(attempt.getStatus())
            .result(PastPaperAttemptResultResponseDto.from(totalAggregator))
            .build();
    }
}
