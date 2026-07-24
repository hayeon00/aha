package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.enums.ExamPartCode;
import com.aha.domain.pastpaper.aggregation.PartResultAggregator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PartResultResponseDto(

    ExamPartCode code,
    String name,
    int userScore,
    int maxScore,
    List<SectionResultResponseDto> sectionResults
) {

    public static PartResultResponseDto from(PartResultAggregator partResultAggregator) {

        List<SectionResultResponseDto> sectionResults = partResultAggregator.getSectionAggregators().stream()
            .map(SectionResultResponseDto::from).toList();

        ExamPart part = partResultAggregator.getPart();

        return PartResultResponseDto.builder()
            .code(part.getCode())
            .name(part.getName())
            .userScore(partResultAggregator.getUserScore())
            .maxScore(part.getTotalScore())
            .sectionResults(sectionResults)
            .build();
    }
}
