package com.aha.domain.user.service;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.entity.ExamVersionStatus;
import com.aha.domain.exam.repository.ExamVersionRepository;
import com.aha.domain.user.dto.response.UserExamResponseDto;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.repository.UserRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExamService {

    private final UserRepository userRepository;
    private final ExamVersionRepository examVersionRepository;
    private final UserExamRepository userExamRepository;

    @Transactional
    public void syncSupportedExamsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<ExamVersion> activeExamVersions =
                examVersionRepository.findAllByStatusOrderByExam_IdAscVersionNoDesc(
                    ExamVersionStatus.ACTIVE);

        for (ExamVersion examVersion : activeExamVersions) {
            boolean alreadyExists = userExamRepository.existsByUser_IdAndExamVersion_Id(
                    user.getId(),
                    examVersion.getId()
            );

            if (alreadyExists) {
                continue;
            }

            UserExam userExam = UserExam.builder()
                    .user(user)
                    .examVersion(examVersion)
                    .isHidden(false)
                    .build();

            userExamRepository.save(userExam);
        }
    }

    public List<UserExamResponseDto> getMyUserExams(Long userId) {
        return userExamRepository.findAllWithExamByUserId(userId)
                .stream()
                .map(UserExamResponseDto::from)
                .toList();
    }

    public List<UserExamResponseDto> getMyVisibleUserExams(Long userId) {
        return userExamRepository.findVisibleWithExamByUserId(userId)
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
        UserExam userExam = userExamRepository.findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));

        userExam.updateHidden(hidden);

        return UserExamResponseDto.from(userExam);
    }


}