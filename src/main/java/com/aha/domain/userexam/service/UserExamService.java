package com.aha.domain.userexam.service;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.entity.ExamVersionStatus;
import com.aha.domain.exam.enums.ExamStatus;
import com.aha.domain.exam.repository.ExamVersionRepository;
import com.aha.domain.userexam.dto.response.UserExamResponseDto;
import com.aha.domain.user.entity.User;
import com.aha.domain.userexam.entity.UserExam;
import com.aha.domain.userexam.repository.UserExamRepository;
import com.aha.domain.user.repository.UserRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExamService {

    private final UserRepository userRepository;
    private final UserExamRepository userExamRepository;
    private final ExamVersionRepository examVersionRepository;

    @Transactional
    public List<UserExamResponseDto> completeOnboarding(
            Long userId,
            Set<Long> selectedExamIds
    ) {
        validateSelectedExamIds(selectedExamIds);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (user.isExamOnboardingCompleted()) {
            throw new BusinessException(
                    ErrorCode.USER_EXAM_ALREADY_CONFIGURED
            );
        }

        List<ExamVersion> latestActiveVersions =
                examVersionRepository
                        .findLatestActiveVersionsByExamIds(
                                selectedExamIds,
                                ExamStatus.ACTIVE,
                                ExamVersionStatus.ACTIVE
                        );

        Map<Long, ExamVersion> versionByExamId =
                latestActiveVersions.stream()
                        .collect(
                                Collectors.toMap(
                                        version ->
                                                version.getExam()
                                                        .getId(),
                                        Function.identity()
                                )
                        );

        if (!versionByExamId.keySet()
                .equals(selectedExamIds)) {
            throw new BusinessException(
                    ErrorCode.EXAM_NOT_ACTIVE
            );
        }

        List<UserExam> userExams =
                selectedExamIds.stream()
                        .sorted()
                        .map(examId ->
                                UserExam.builder()
                                        .user(user)
                                        .examVersion(
                                                versionByExamId
                                                        .get(examId)
                                        )
                                        .isHidden(false)
                                        .build()
                        )
                        .toList();

        List<UserExam> savedUserExams = userExamRepository.saveAll(userExams);

        user.completeExamOnboarding();

        return savedUserExams.stream()
                .map(UserExamResponseDto::from)
                .toList();
    }

    public List<UserExamResponseDto> getMyUserExams(
            Long userId
    ) {
        return userExamRepository
                .findAllWithExamByUserId(userId)
                .stream()
                .map(UserExamResponseDto::from)
                .toList();
    }

    public List<UserExamResponseDto> getMyVisibleUserExams(
            Long userId
    ) {
        return userExamRepository
                .findVisibleWithExamByUserId(userId)
                .stream()
                .map(UserExamResponseDto::from)
                .toList();
    }

    @Transactional
    public List<UserExamResponseDto> addUserExams(
            Long userId,
            Set<Long> selectedExamIds
    ) {
        validateSelectedExamIds(selectedExamIds);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        Set<Long> existingExamIds =
                userExamRepository.findExamIdsByUserId(userId);
        Set<Long> newExamIds = selectedExamIds.stream()
                .filter(examId -> !existingExamIds.contains(examId))
                .collect(Collectors.toSet());

        if (newExamIds.isEmpty()) {
            return List.of();
        }

        List<ExamVersion> latestActiveVersions =
                examVersionRepository.findLatestActiveVersionsByExamIds(
                        newExamIds,
                        ExamStatus.ACTIVE,
                        ExamVersionStatus.ACTIVE
                );

        Map<Long, ExamVersion> versionByExamId =
                latestActiveVersions.stream()
                        .collect(Collectors.toMap(
                                version -> version.getExam().getId(),
                                Function.identity()
                        ));

        if (!versionByExamId.keySet().equals(newExamIds)) {
            throw new BusinessException(ErrorCode.EXAM_NOT_ACTIVE);
        }

        List<UserExam> userExams = newExamIds.stream()
                .sorted()
                .map(examId -> UserExam.builder()
                        .user(user)
                        .examVersion(versionByExamId.get(examId))
                        .isHidden(false)
                        .build())
                .toList();

        return userExamRepository.saveAll(userExams)
                .stream()
                .map(UserExamResponseDto::from)
                .toList();
    }

    @Transactional
    public UserExamResponseDto updateHidden(
            Long userId,
            Long userExamId,
            Boolean hidden
    ) {
        if (hidden == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        UserExam userExam =
                userExamRepository
                        .findByIdAndUser_Id(
                                userExamId,
                                userId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_EXAM_NOT_FOUND
                                )
                        );

        userExam.updateHidden(hidden);

        return UserExamResponseDto.from(
                userExam
        );
    }

    private void validateSelectedExamIds(
            Set<Long> selectedExamIds
    ) {
        if (selectedExamIds == null
                || selectedExamIds.isEmpty()
                || selectedExamIds.contains(null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }
}
