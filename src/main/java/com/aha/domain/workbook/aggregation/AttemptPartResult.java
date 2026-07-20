package com.aha.domain.workbook.aggregation;

import com.aha.domain.exam.entity.ExamPart;
import java.util.List;
import lombok.Builder;

@Builder
public record AttemptPartResult(

    Long id,
    String name,
    int totalScore,
    int userScore,
    int displayOrder,
    List<AttemptSectionResult> sectionResults
) {

    public static AttemptPartResult create(ExamPart examPart,List<AttemptSectionResult> sectionResults) {

        int totalScore = sectionResults.stream().mapToInt(AttemptSectionResult::totalScore).sum();
        int userScore = sectionResults.stream().mapToInt(AttemptSectionResult::userScore).sum();
        return AttemptPartResult.builder()
            .id(examPart.getId())
            .name(examPart.getName())
            .totalScore(totalScore)
            .userScore(userScore)
            .displayOrder(examPart.getDisplayOrder())
            .sectionResults(sectionResults)
            .build();
    }
}
