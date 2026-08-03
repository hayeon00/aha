package com.aha.domain.study.dto.response;

import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.enums.StudyRoomStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record StudyRoomCreateResponseDto(

    long studyRoomId,
    StudyRoomStatus status,
    LocalDateTime createdAt
) {

    public static StudyRoomCreateResponseDto from(StudyRoom studyRoom){

        return StudyRoomCreateResponseDto.builder()
            .studyRoomId(studyRoom.getId())
            .status(studyRoom.getStatus())
            .createdAt(studyRoom.getCreatedAt())
            .build();
    }
}
