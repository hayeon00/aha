package com.aha.domain.study.service;

import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.repository.PastPaperRepository;
import com.aha.domain.study.dto.request.StudyRoomCreateRequestDto;
import com.aha.domain.study.dto.response.StudyRoomCreateResponseDto;
import com.aha.domain.study.dto.response.StudyRoomDetailResponseDto;
import com.aha.domain.study.dto.response.StudyRoomResponseDto;
import com.aha.domain.study.entity.ActiveStudyRoomParticipation;
import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.entity.StudyRoomMember;
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
    private final ActiveStudyRoomParticipationRepository activeStudyRoomParticipationRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;

    @Transactional
    public StudyRoomCreateResponseDto createStudyRoom(
        StudyRoomCreateRequestDto requestDto,
        Long userId
    ) {

        if (activeStudyRoomParticipationRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.STUDY_PARTICIPATION_ALREADY_EXISTS);
        }

        PastPaper paper = pastPaperRepository.findById(requestDto.pastPaperId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_NOT_FOUND));

        paper.validatePublished();

        User createdBy = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.save(StudyRoom.create(
            paper, createdBy, requestDto.title(),
            requestDto.description(), requestDto.capacity(), requestDto.timeLimit())
        );

        try {

            activeStudyRoomParticipationRepository.saveAndFlush(
                ActiveStudyRoomParticipation.create(userId, studyRoom)
            );
        } catch (DataIntegrityViolationException e) {

            if (isActiveParticipationUniqueViolation(e)) {

                throw new BusinessException(ErrorCode.STUDY_PARTICIPATION_ALREADY_EXISTS);
            }

            throw e;
        }

        studyRoomMemberRepository.save(StudyRoomMember.create(createdBy, studyRoom));

        return StudyRoomCreateResponseDto.from(studyRoom);

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
            studyRoomRepository.findStudyRooms(
                examVersionId,
                status,
                pageable
            ).map(StudyRoomResponseDto::create);

        return PageResponseDto.from(result);
    }

    @Transactional(readOnly = true)
    public StudyRoomDetailResponseDto getStudyRoom(Long studyRoomId) {

        StudyRoom studyRoom = studyRoomRepository.findStudyRoom(studyRoomId)
            .orElseThrow(()->new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        studyRoom.validateNotCanceled();

        return StudyRoomDetailResponseDto.from(studyRoom);
    }

    @Transactional(readOnly = true)
    public StudyRoomDetailResponseDto getCurrentStudyRoom(Long userId) {

        ActiveStudyRoomParticipation participation
            =activeStudyRoomParticipationRepository.findByUserId(userId)
            .orElseThrow(()->new BusinessException(ErrorCode.JOINED_STUDY_ROOM_NOT_FOUND));

        Long studyRoomId = participation.getStudyRoom().getId();

        StudyRoom studyRoom = studyRoomRepository.findStudyRoom(studyRoomId)
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

        return StudyRoomDetailResponseDto.from(studyRoom);
    }
}
