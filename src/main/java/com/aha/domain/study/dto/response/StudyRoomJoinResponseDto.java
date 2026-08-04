package com.aha.domain.study.dto.response;

import com.aha.domain.study.entity.StudyRoom;

public record StudyRoomJoinResponseDto(

    long studyRoomId
) {

    public static StudyRoomJoinResponseDto from(StudyRoom studyRoom) {

        return new StudyRoomJoinResponseDto(studyRoom.getId());
    }
}
