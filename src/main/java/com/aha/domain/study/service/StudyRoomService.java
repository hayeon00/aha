package com.aha.domain.study.service;

import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.repository.PastPaperAttemptRepository;
import com.aha.domain.pastpaper.repository.PastPaperRepository;
import com.aha.domain.study.dto.request.StudyRoomCreateRequestDto;
import com.aha.domain.study.dto.response.StudyRoomAttemptStartResponseDto;
import com.aha.domain.study.dto.response.StudyRoomCreateResponseDto;
import com.aha.domain.study.dto.response.StudyRoomDetailResponseDto;
import com.aha.domain.study.dto.response.StudyRoomJoinResponseDto;
import com.aha.domain.study.dto.response.StudyRoomResponseDto;
import com.aha.domain.study.entity.ActiveStudyRoomParticipation;
import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.entity.StudyRoomMember;
import com.aha.domain.study.enums.StudyRoomMemberRole;
import com.aha.domain.study.enums.StudyRoomSortType;
import com.aha.domain.study.enums.StudyRoomStatus;
import com.aha.domain.study.repository.ActiveStudyRoomParticipationRepository;
import com.aha.domain.study.repository.StudyRoomMemberRepository;
import com.aha.domain.study.repository.StudyRoomRepository;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.repository.UserRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.response.PageResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyRoomService {

    private static final String ACTIVE_PARTICIPATION_UNIQUE_CONSTRAINT = "uk_active_study_room_participation_user_id";

    private final PastPaperRepository pastPaperRepository;
    private final ActiveStudyRoomParticipationRepository participationRepository;
    private final StudyRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final StudyRoomMemberRepository memberRepository;
    private final PastPaperAttemptRepository attemptRepository;

    @Transactional
    public StudyRoomCreateResponseDto createStudyRoom(
        StudyRoomCreateRequestDto requestDto,
        Long userId
    ) {

        if (participationRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.STUDY_PARTICIPATION_ALREADY_EXISTS);
        }

        PastPaper paper = pastPaperRepository.findById(requestDto.pastPaperId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_NOT_FOUND));

        paper.validatePublished();

        User createdBy = userRepository.findById(userId)
            .orElseThrow(() ->

                new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "인증된 사용자 정보를 DB에서 찾을 수 없습니다. userId=" + userId
                )
            );

        StudyRoom room = roomRepository.save(StudyRoom.create(
            paper, createdBy, requestDto.title(),
            requestDto.description(), requestDto.capacity(), requestDto.timeLimit())
        );

        registerStudyRoomMember(createdBy, room, StudyRoomMemberRole.HOST);

        return StudyRoomCreateResponseDto.from(room);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<StudyRoomResponseDto> getStudyRooms(
        long examVersionId, StudyRoomStatus status, int page, int size, StudyRoomSortType sortType
    ) {

        Pageable pageable = PageRequest.of(
            page,
            size,
            sortType.getSort()
        );

        Page<StudyRoomResponseDto> result =
            roomRepository.findStudyRooms(
                examVersionId,
                status,
                pageable
            ).map(StudyRoomResponseDto::create);

        return PageResponseDto.from(result);
    }

    @Transactional(readOnly = true)
    public StudyRoomDetailResponseDto getStudyRoom(Long studyRoomId, Long userId) {

        StudyRoom studyRoom = roomRepository.findStudyRoom(studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        studyRoom.validateNotCanceled();

        return StudyRoomDetailResponseDto.from(studyRoom, userId);
    }

    @Transactional(readOnly = true)
    public StudyRoomDetailResponseDto getCurrentStudyRoom(Long userId) {

        ActiveStudyRoomParticipation participation
            = participationRepository.findByUserIdWithStudyRoom(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOINED_STUDY_ROOM_NOT_FOUND));

        Long studyRoomId = participation.getStudyRoom().getId();

        StudyRoom studyRoom = roomRepository.findStudyRoom(studyRoomId)
            .orElseThrow(() -> {
                log.error(
                    "스터디룸 정합성 오류: active participation은 존재하지만 room 상세 조회 실패. userId={}, studyRoomId={}",
                    userId,
                    studyRoomId
                );

                return new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
                );
            });

        return StudyRoomDetailResponseDto.from(studyRoom, userId);
    }

    @Transactional
    public StudyRoomJoinResponseDto joinStudyRoom(Long userId, Long studyRoomId) {

        StudyRoom room = roomRepository.findByIdForUpdate(studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        if (room.isWaiting() && participationRepository.existsByUserId(
            userId)) {

            throw new BusinessException(ErrorCode.STUDY_PARTICIPATION_ALREADY_EXISTS);
        }

        if (memberRepository.existsByStudyRoom_IdAndUser_Id(studyRoomId, userId)) {

            throw new BusinessException(ErrorCode.STUDY_ROOM_ALREADY_JOINED);
        }

        room.validateCanJoin(

            memberRepository.countByStudyRoom_Id(studyRoomId)
        );

        User user = userRepository.findById(userId)
            .orElseThrow(() ->

                new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "인증된 사용자 정보를 DB에서 찾을 수 없습니다. userId=" + userId
                )
            );

        registerStudyRoomMember(user, room, StudyRoomMemberRole.MEMBER);

        return StudyRoomJoinResponseDto.from(room);
    }

    private void registerStudyRoomMember(User user, StudyRoom room, StudyRoomMemberRole role) {

        if (room.isWaiting()) {

            try {

                participationRepository.saveAndFlush(
                    ActiveStudyRoomParticipation.create(user.getId(), room)
                );
            } catch (DataIntegrityViolationException e) {

                if (isActiveParticipationUniqueViolation(e)) {

                    throw new BusinessException(ErrorCode.STUDY_PARTICIPATION_ALREADY_EXISTS);
                }

                throw e;
            }
        }

        memberRepository.save(StudyRoomMember.create(user, room, role));
    }

    private boolean isActiveParticipationUniqueViolation(
        DataIntegrityViolationException exception
    ) {

        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException constraintException) {

                return ACTIVE_PARTICIPATION_UNIQUE_CONSTRAINT.equals(
                    constraintException.getConstraintName()
                );
            }

            cause = cause.getCause();
        }

        return false;
    }

    @Transactional
    public StudyRoomAttemptStartResponseDto startStudyRoom(Long roomId, Long userId) {

        StudyRoom room = roomRepository.findByIdForUpdate(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        StudyRoomMember maybeHost = memberRepository.findByStudyRoom_IdAndUser_Id(
                room.getId(), userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REQUESTER_NOT_STUDY_ROOM_MEMBER));

        maybeHost.validateCanStartRoom();

        List<StudyRoomMember> members = memberRepository.findByStudyRoom_IdWithUser(roomId);

        room.validateCanStart();

        LocalDateTime startTime = LocalDateTime.now();

        List<PastPaperAttempt> newAttempts = members
            .stream().map(

                member ->
                    PastPaperAttempt.create(
                        room.getTimeLimit(),
                        member.getUser().getId(),
                        room.getPastPaper(),
                        startTime
                    )
            ).toList();

        for (int i = 0; i < members.size(); i++) {

            members.get(i).assignAttempt(newAttempts.get(i));
        }

        attemptRepository.saveAll(newAttempts);

        room.updateStatusAfterStart();

        return StudyRoomAttemptStartResponseDto.from(maybeHost.getPastPaperAttempt());
    }
}
