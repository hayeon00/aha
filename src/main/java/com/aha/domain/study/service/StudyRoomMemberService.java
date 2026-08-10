package com.aha.domain.study.service;

import com.aha.domain.study.dto.request.StudyRoomHostChangeRequestDto;
import com.aha.domain.study.dto.request.StudyRoomReadyUpdateRequestDto;
import com.aha.domain.study.entity.ActiveStudyRoomParticipation;
import com.aha.domain.study.entity.StudyRoom;
import com.aha.domain.study.entity.StudyRoomMember;
import com.aha.domain.study.enums.StudyRoomMemberRole;
import com.aha.domain.study.repository.ActiveStudyRoomParticipationRepository;
import com.aha.domain.study.repository.StudyRoomMemberRepository;
import com.aha.domain.study.repository.StudyRoomRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyRoomMemberService {

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;
    private final ActiveStudyRoomParticipationRepository activeStudyRoomParticipationRepository;

    @Transactional
    public void leaveStudyRoom(Long studyRoomId, Long userId) {

        StudyRoom studyRoom = studyRoomRepository.findByIdForUpdate(studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        studyRoom.validateCanLeave();

        StudyRoomMember member = studyRoomMemberRepository.findByStudyRoom_IdAndUser_Id(
                studyRoom.getId(), userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REQUESTER_NOT_STUDY_ROOM_MEMBER));

        member.validateForLeave();

        if (studyRoom.isWaiting()) {

            activeStudyRoomParticipationRepository.findByStudyRoom_IdAndUserId(studyRoom.getId(),
                    userId)
                .ifPresent(

                    activeStudyRoomParticipationRepository::delete
                );
        }

        studyRoomMemberRepository.delete(member);
    }

    @Transactional
    public void kickMember(Long studyRoomId, Long memberId, Long userId) {

        StudyRoom studyRoom = studyRoomRepository.findByIdForUpdate(studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        studyRoom.validateCanKick();

        StudyRoomMember member = studyRoomMemberRepository.findByIdAndStudyRoom_IdWithUser(memberId,
                studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_TARGET_NOT_FOUND));

        StudyRoomMember maybeHost = studyRoomMemberRepository.findByStudyRoom_IdAndUser_Id(
                studyRoom.getId(), userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REQUESTER_NOT_STUDY_ROOM_MEMBER));

        maybeHost.validateForKick(member);

        activeStudyRoomParticipationRepository.findByStudyRoom_IdAndUserId(studyRoom.getId(),
                member.getUser().getId())
            .ifPresent(

                activeStudyRoomParticipationRepository::delete
            );

        studyRoomMemberRepository.delete(member);
    }

    @Transactional
    public void changeHost(Long studyRoomId, StudyRoomHostChangeRequestDto requestDto,
        Long userId) {

        StudyRoom studyRoom = studyRoomRepository.findByIdForUpdate(studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        studyRoom.validateCanChangeHost();

        Long memberId = requestDto.studyRoomMemberId();

        StudyRoomMember member = studyRoomMemberRepository.findByIdAndStudyRoom_IdWithUser(memberId,
                studyRoomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_TARGET_NOT_FOUND));

        StudyRoomMember maybeHost = studyRoomMemberRepository.findByStudyRoom_IdAndUser_Id(
                studyRoom.getId(), userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REQUESTER_NOT_STUDY_ROOM_MEMBER));

        maybeHost.validateForChangeHost(member);

        member.updateRole(StudyRoomMemberRole.HOST);

        maybeHost.updateRole(StudyRoomMemberRole.MEMBER);
    }

    @Transactional
    public void updateReady(StudyRoomReadyUpdateRequestDto requestDto, Long userId) {

        ActiveStudyRoomParticipation participation = activeStudyRoomParticipationRepository.findByUserIdWithStudyRoom(
                userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOINED_STUDY_ROOM_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.findByIdForUpdate(participation.getStudyRoom().getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_ROOM_NOT_FOUND));

        studyRoom.validateCanUpdateReady();

        StudyRoomMember member = studyRoomMemberRepository.findByStudyRoom_IdAndUser_Id(
                studyRoom.getId(), userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REQUESTER_NOT_STUDY_ROOM_MEMBER));

        member.updateReady(requestDto.ready());
    }
}
