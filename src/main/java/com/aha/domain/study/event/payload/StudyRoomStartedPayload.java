package com.aha.domain.study.event.payload;

import com.aha.domain.study.event.application.StudyRoomStartedApplicationEvent;
import java.time.LocalDateTime;

public record StudyRoomStartedPayload(

    String type,
    Long studyRoomId,
    LocalDateTime startedAt,
    LocalDateTime dueAt
) {

    private static final String EVENT_TYPE =
        "STUDY_ROOM_STARTED";

    public static StudyRoomStartedPayload from(
        StudyRoomStartedApplicationEvent event
    ) {
        return new StudyRoomStartedPayload(
            EVENT_TYPE,
            event.studyRoomId(),
            event.startedAt(),
            event.dueAt()
        );
    }

}
