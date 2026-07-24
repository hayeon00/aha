package com.aha.domain.pastpaper.aggregation;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.pastpaper.enums.PastPaperFailureReason;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PRIVATE)
@Getter
public class TotalResultAggregator {

    private ExamVersion version;
    private int userScore;
    private boolean isPassed;

    @Builder.Default
    Set<PastPaperFailureReason> failureReasons = EnumSet.noneOf(PastPaperFailureReason.class);
    @Builder.Default
    List<PartResultAggregator> failedPartAggregators = new ArrayList<>();
    @Builder.Default
    private List<PartResultAggregator> partAggregators = new ArrayList<>();

    public static TotalResultAggregator create(ExamVersion version) {
        return TotalResultAggregator.builder().version(version).userScore(0).build();
    }

    public void aggregate(PartResultAggregator partAggregator) {
        userScore += partAggregator.getUserScore();
        partAggregators.add(partAggregator);
    }

    public void evaluate() {
        boolean totalScorePassed = userScore >= version.getPassingScore();

        if (!totalScorePassed) {
            failureReasons.add(PastPaperFailureReason.TOTAL_SCORE_BELOW_MINIMUM);
        }

        partAggregators.forEach(part->{
            part.evaluate();
            if(!part.isPartPassed()) {
                failedPartAggregators.add(part);
            }
        });

        boolean hasFailedPart = partAggregators.stream().anyMatch(part -> !part.isPartPassed());

        if (hasFailedPart) {
            failureReasons.add(PastPaperFailureReason.SUBJECT_SCORE_BELOW_MINIMUM);
        }

        isPassed = totalScorePassed && !hasFailedPart;
    }
}
