package com.aha.domain.pastpaper.aggregation;

import com.aha.domain.exam.entity.ExamPart;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PartResultAggregator {

    private ExamPart part;
    private int userScore;
    private boolean isPartPassed;

    @Builder.Default
    private List<SectionResultAggregator> sectionAggregators = new ArrayList<>();

    public static PartResultAggregator from(ExamPart part) {
        return PartResultAggregator.builder()
            .part(part)
            .userScore(0)
            .build();
    }

    public void aggregate(SectionResultAggregator sectionAggregator) {
        userScore += sectionAggregator.getUserScore();
        sectionAggregators.add(sectionAggregator);
    }

    public void evaluate() {

        if (!part.isSubjectFailTarget()) {
            isPartPassed = true;
            return;
        }

        Integer threshold = part.getSubjectFailThresholdScore();

        if (threshold == null) {
            throw new IllegalStateException(
                "과락 대상 Part에 기준 점수가 없습니다: "
                    + part.getId()
            );
        }

        if (threshold <= userScore) {
            isPartPassed = true;
        }
    }
}
