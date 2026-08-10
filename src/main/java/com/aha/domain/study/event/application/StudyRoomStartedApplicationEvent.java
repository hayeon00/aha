package com.aha.domain.study.event.application;

import java.time.LocalDateTime;

public record StudyRoomStartedApplicationEvent(

    Long studyRoomId,
    LocalDateTime startedAt,
    LocalDateTime dueAt
) {

}
