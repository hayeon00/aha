package com.aha.domain.pastpaper.aggregation;


import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.pastpaper.entity.UserAnswer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SectionResultAggregator {

    private ExamScopeNode node;
    private ExamPart part;
    private int userScore;
    private int maxScore;

    public static SectionResultAggregator from(ExamScopeNode scopeNode) {

        return SectionResultAggregator.builder()
            .node(scopeNode)
            .part(scopeNode.getExamPart())
            .userScore(0)
            .maxScore(0)
            .build();
    }

    public void aggregate(UserAnswer userAnswer) {
        int score = userAnswer.getProblem().getScore();
        if (userAnswer.getCorrect()) {
            userScore += score;
        }
        maxScore += score;
    }
}
