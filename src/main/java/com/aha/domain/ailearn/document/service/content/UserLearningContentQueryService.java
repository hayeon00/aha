package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.dto.content.response.UserLearningContentResponseDto;
import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.userexam.entity.UserExam;
import com.aha.domain.userexam.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLearningContentQueryService {

    private final UserExamRepository userExamRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final UserLearningContentRepository userLearningContentRepository;

    /**
     * 특정 사용자 시험에 생성된 전체 개념설명을
     * 시험 목차 표시 순서대로 조회한다.
     */
    public List<UserLearningContentResponseDto> getLearningContents(
            Long userId,
            Long userExamId
    ) {
        validateRequest(
                userId,
                userExamId
        );

        validateUserExamOwnership(
                userId,
                userExamId
        );

        return userLearningContentRepository
                .findAllByUserExam_IdOrderByExamScopeNode_DisplayOrderAsc(
                        userExamId
                )
                .stream()
                .map(UserLearningContentResponseDto::from)
                .toList();
    }

    /**
     * 특정 사용자 시험의 특정 시험 목차에 생성된
     * 개념설명을 조회한다.
     */
    public UserLearningContentResponseDto getLearningContent(
            Long userId,
            Long userExamId,
            Long examScopeNodeId
    ) {
        validateRequest(
                userId,
                userExamId,
                examScopeNodeId
        );

        UserExam userExam = userExamRepository
                .findByIdAndUser_Id(userExamId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_EXAM_NOT_FOUND));
        ExamScopeNode scopeNode = examScopeNodeRepository.findById(examScopeNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND));
        if (!scopeNode.getExamVersion().getId().equals(userExam.getExamVersion().getId())) {
            throw new BusinessException(ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND);
        }

        UserLearningContent learningContent = userLearningContentRepository
                .findByUserExam_IdAndExamScopeNode_IdAndUserExam_User_Id(
                        userExamId, examScopeNodeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_CONTENT_NOT_FOUND));
        return UserLearningContentResponseDto.from(learningContent);
    }

    /**
     * 요청한 사용자 시험이 현재 로그인 사용자의 것인지 확인한다.
     */
    private void validateUserExamOwnership(
            Long userId,
            Long userExamId
    ) {
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
    }

    private void validateRequest(
            Long userId,
            Long userExamId
    ) {
        if (userId == null || userExamId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (userId <= 0 || userExamId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private void validateRequest(
            Long userId,
            Long userExamId,
            Long examScopeNodeId
    ) {
        validateRequest(
                userId,
                userExamId
        );

        if (examScopeNodeId == null
                || examScopeNodeId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }
}
