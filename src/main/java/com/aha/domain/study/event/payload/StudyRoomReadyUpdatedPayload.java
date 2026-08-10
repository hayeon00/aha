package com.aha.domain.study.event.payload;

import com.aha.domain.study.event.application.StudyRoomReadyUpdatedApplicationEvent;

public record StudyRoomReadyUpdatedPayload(

    String type,
    Long studyRoomId,
    Long memberId,
    boolean ready
) {

    private static final String EVENT_TYPE
        ="STUDY_ROOM_MEMBER_READY_UPDATED";

    public static StudyRoomReadyUpdatedPayload from(
        StudyRoomReadyUpdatedApplicationEvent event
    ){

        return new StudyRoomReadyUpdatedPayload(
            EVENT_TYPE,
            event.studyRoomId(),
            event.memberId(),
            event.ready()
        );
    }
}
