package com.aha.domain.study.event.application;

public record StudyRoomReadyUpdatedApplicationEvent(

    Long studyRoomId,
    Long memberId,
    boolean ready
) {

}
