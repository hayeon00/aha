package com.aha.domain.study.dto.response;

import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.entity.StudyRoomMember;
import com.aha.domain.study.enums.StudyRoomMemberRole;
import com.aha.domain.study.enums.StudyRoomStatus;
import com.aha.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record StudyRoomDetailResponseDto(

    long id,
    String title,
    String description,
    int memberCount,
    int capacity,
    StudyRoomStatus status,
    LocalDateTime createdAt,
    boolean updated,
    LocalDateTime updatedAt,
    int timeLimit,

    List<StudyRoomMemberResponseDto> members,
    PastPaperResponseDto pastPaper
) {

    record StudyRoomMemberResponseDto(

        long memberId,
        String nickname,
        String profileImageUrl,
        StudyRoomMemberRole role,
        boolean ready,
        boolean me
    ) {

        public static StudyRoomMemberResponseDto from(StudyRoomMember member, Long requesterId) {

            User user = member.getUser();
            return new StudyRoomMemberResponseDto(
                member.getId(), user.getNickname(), user.getProfileImageUrl(), member.getRole(), member.isReady(),
                Objects.equals(member.getUser().getId(), requesterId)
            );
        }
    }

    record PastPaperResponseDto(

        String title,
        int totalItemCount,
        LocalDate examDate
    ) {

        public static PastPaperResponseDto from(PastPaper paper) {

            return new PastPaperResponseDto(
                createTitle(paper.getExamVersion().getExam().getName(), paper.getYear(),
                    paper.getRoundNo()),
                paper.getTotalItemCount(),
                paper.getExamDate()
            );
        }

        private static String createTitle(String examName, int year, int roundNo) {
            return "%s %d년 %d회차 시험".formatted(examName, year, roundNo);
        }
    }

    public static StudyRoomDetailResponseDto from(StudyRoom room, Long requesterId) {

        List<StudyRoomMember> members = room.getMembers();

        return StudyRoomDetailResponseDto.builder()
            .id(room.getId())
            .title(room.getTitle())
            .description(room.getDescription())
            .memberCount(members.size())
            .capacity(room.getCapacity())
            .status(room.getStatus())
            .createdAt(room.getCreatedAt())
            .updated(!room.getCreatedAt().equals(room.getUpdatedAt()))
            .updatedAt(room.getUpdatedAt())
            .timeLimit(room.getTimeLimit())
            .members(
                members.stream().map(member -> StudyRoomMemberResponseDto.from(member, requesterId))
                    .toList())
            .pastPaper(PastPaperResponseDto.from(room.getPastPaper()))
            .build();
    }
}