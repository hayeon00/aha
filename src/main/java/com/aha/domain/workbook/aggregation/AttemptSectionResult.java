package com.aha.domain.workbook.aggregation;

import com.aha.domain.exam.entity.ExamScopeNode;
import lombok.Builder;

@Builder
public record AttemptSectionResult(

    Long id,
    String title,
    Integer totalScore,
    Integer userScore,
    Integer totalQuestionCount,
    Integer correctQuestionCount,
    Integer displayOrder
) {

    public static AttemptSectionResult create(SectionAggregator aggregator){

        ExamScopeNode node = aggregator.getExamScopeNode();
        return AttemptSectionResult.builder()
            .id(node.getId())
            .title(node.getTitle())
            .totalScore(aggregator.getTotalScore())
            .userScore(aggregator.getUserScore())
            .totalQuestionCount(aggregator.getTotalQuestionCount())
            .correctQuestionCount(aggregator.getCorrectQuestionCount())
            .displayOrder(node.getDisplayOrder())
            .build();
    }
}
