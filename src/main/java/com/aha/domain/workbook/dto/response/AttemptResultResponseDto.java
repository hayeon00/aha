package com.aha.domain.workbook.dto.response;

import com.aha.domain.workbook.aggregation.AttemptPartResult;
import com.aha.domain.workbook.aggregation.AttemptStatJson;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import java.util.List;
import lombok.Builder;

@Builder
public record AttemptResultResponseDto(

    Integer totalScore,
    Integer userScore,
    Boolean passed,
    String resultMessage,
    Integer elapsedTime,

    List<AttemptPartResult> partResults
) {
    public static AttemptResultResponseDto from(WorkbookAttempt workbookAttempt){

        AttemptStatJson stat = workbookAttempt.getStatJson();
        int totalScore = stat.partResults().stream().mapToInt(AttemptPartResult::totalScore).sum();
        int userScore = stat.partResults().stream().mapToInt(AttemptPartResult::userScore).sum();

        return AttemptResultResponseDto.builder()
            .totalScore(totalScore)
            .userScore(userScore)
            .passed(workbookAttempt.getIsPassed())
            .partResults(stat.partResults())
            .resultMessage(workbookAttempt.getResultMessage())
            .elapsedTime(workbookAttempt.getElapsedTime())
            .build();
    }
}
