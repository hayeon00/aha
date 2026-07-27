package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.pastpaper.entity.PastPaper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperResponseDto(

    long pastPaperId,
    long totalItemCount,
    boolean reviewed,
    String title,
    long year,
    long roundNo,
    long timeLimit,
    LocalDate examDate,

    Long solvingAttemptId,
    LocalDateTime attemptStartedAt,
    LocalDateTime attemptDueAt
) {

    public static PastPaperResponseDto from(PastPaper paper, Long solvingAttemptId, LocalDateTime attemptStartedAt) {

        LocalDateTime attemptDueAt = attemptStartedAt == null ? null : attemptStartedAt.plusSeconds(paper.getTimeLimit());

        return PastPaperResponseDto.builder()
            .pastPaperId(paper.getId())
            .totalItemCount(paper.getTotalItemCount())
            .reviewed(paper.isReviewed())
            .title(paper.createTitle())
            .year(paper.getYear())
            .roundNo(paper.getRoundNo())
            .timeLimit(paper.getTimeLimit())
            .examDate(paper.getExamDate())
            .solvingAttemptId(solvingAttemptId)
            .attemptStartedAt(attemptStartedAt)
            .attemptDueAt(attemptDueAt)
            .build();
    }


}
