package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.pastpaper.aggregation.PartResultAggregator;
import com.aha.domain.pastpaper.enums.PastPaperFailureReason;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PartScoreFailureResponseDto(

    PastPaperFailureReason code,
    String name,
    List<FailedPartResponseDto> failedParts
) implements FailureReasonResponseDto {

    public static PartScoreFailureResponseDto from(List<PartResultAggregator> partAggregators) {

        PastPaperFailureReason code =PastPaperFailureReason.SUBJECT_SCORE_BELOW_MINIMUM;
        List<FailedPartResponseDto> failedParts =partAggregators.stream().map(
            FailedPartResponseDto::from).toList();
        return PartScoreFailureResponseDto.builder()
            .code(code)
            .name(code.getName())
            .failedParts(failedParts)
            .build();
    }
}
