package com.aha.domain.workbook.aggregation;

import com.aha.domain.exam.entity.ExamScopeNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SectionAggregator {

    private ExamScopeNode examScopeNode;
    private int totalScore;
    private int totalQuestionCount;
    private int userScore;
    private int correctQuestionCount;


    public static SectionAggregator create(ExamScopeNode examScopeNode) {
        return SectionAggregator.builder()
            .examScopeNode(examScopeNode)
            .build();
    }

    public void increaseUserScore(int problemScore){
        userScore += problemScore;
    }

    public void increaseCorrectQuestionCount(){
        correctQuestionCount++;
    }


    public void increaseTotalScore(int problemScore) {
        totalScore+=problemScore;
    }
    public void increaseTotalQuestionCount(){
        totalQuestionCount++;
    }
}
