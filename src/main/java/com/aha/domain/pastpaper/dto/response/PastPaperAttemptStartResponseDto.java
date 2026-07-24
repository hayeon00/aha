package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperAttemptStartResponseDto(

    long attemptId,
    PastPaperAttemptStatus attemptStatus,
    LocalDateTime startedAt,
    LocalDateTime dueAt
) {

    public static PastPaperAttemptStartResponseDto from(PastPaperAttempt attempt) {
        PastPaper paper = attempt.getPastPaper();
        LocalDateTime startedAt = attempt.getStartedAt();
        int timeLimit = paper.getTimeLimit();

        return PastPaperAttemptStartResponseDto.builder()
            .attemptId(attempt.getId())
            .attemptStatus(attempt.getStatus())
            .startedAt(startedAt)
            .dueAt(createDueAt(startedAt, timeLimit))
            .build();
    }

    private static LocalDateTime createDueAt(LocalDateTime startedAt, int timeLimit) {
        return startedAt.plusSeconds(timeLimit);
    }
}
