package com.aha.domain.pastpaper.dto.response.result;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.enums.PastPaperStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access= AccessLevel.PRIVATE)
public record PastPaperAttemptResponseDto(

    long attemptId,
    int maxScore,
    Integer userScore,
    Boolean passed,
    Integer elapsedTime,
    LocalDateTime startedAt,
    long pastPaperId,
    String paperTitle,
    PastPaperStatus paperStatus
) {

    public static PastPaperAttemptResponseDto from(PastPaperAttempt attempt){

        PastPaper paper = attempt.getPastPaper();
        ExamVersion version = paper.getExamVersion();
        return PastPaperAttemptResponseDto.builder()
            .attemptId(attempt.getId())
            .maxScore(version.getTotalScore())
            .userScore(attempt.getUserScore())
            .passed(attempt.getPassed())
            .elapsedTime(attempt.getElapsedTime())
            .startedAt(attempt.getStartedAt())
            .pastPaperId(paper.getId())
            .paperTitle(paper.createTitle())
            .paperStatus(paper.getStatus())
            .build();
    }
}
