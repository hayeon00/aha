package com.aha.domain.study.dto.response;

import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.enums.StudyRoomStatus;
import com.aha.domain.study.repository.projection.StudyRoomProjection;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record StudyRoomResponseDto(

    long id,
    String title,
    String description,
    HostResponseDto host,
    int memberCount,
    int capacity,
    StudyRoomStatus status,
    LocalDateTime createdAt,
    boolean updated,
    LocalDateTime updatedAt,
    String pastPaperTitle,
    int timeLimit

) {

    public record HostResponseDto(
        String nickname,
        String profileImageUrl
    ) {

        public static HostResponseDto create(String nickname, String profileImageUrl) {

            return new HostResponseDto(nickname, profileImageUrl);
        }
    }

    public static StudyRoomResponseDto create(StudyRoomProjection projection) {

        StudyRoom room = projection.getStudyRoom();

        return StudyRoomResponseDto.builder()
            .id(room.getId())
            .title(room.getTitle())
            .description(room.getDescription())
            .host(HostResponseDto.create(projection.getHostNickname(),
                projection.getHostProfileImageUrl()))
            .memberCount(Math.toIntExact(projection.getMemberCount()))
            .capacity(room.getCapacity())
            .status(room.getStatus())
            .createdAt(room.getCreatedAt())
            .updated(!room.getUpdatedAt().equals(room.getCreatedAt()))
            .updatedAt(room.getUpdatedAt())
            .pastPaperTitle(createTitle(projection.getExamName(), projection.getPastPaperYear(),
                projection.getPastPaperRoundNo()))
            .timeLimit(room.getTimeLimit())
            .build();
    }

    private static String createTitle(String examName, int year, int roundNo) {
        return "%s %d년 %d회차 시험".formatted(examName, year, roundNo);
    }
}
