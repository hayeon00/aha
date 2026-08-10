package com.aha.domain.study.dto.response;

import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import java.time.LocalDateTime;

public record StudyRoomAttemptStartResponseDto(

    Long pastPaperAttemptId,
    LocalDateTime startedAt,
    LocalDateTime dueAt
) {

    public static StudyRoomAttemptStartResponseDto from(PastPaperAttempt attempt) {

        return new StudyRoomAttemptStartResponseDto(
            attempt.getId(),
            attempt.getStartedAt(),
            attempt.getDueAt()
        );
    }
}
