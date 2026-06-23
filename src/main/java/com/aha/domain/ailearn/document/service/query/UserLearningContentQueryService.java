package com.aha.domain.ailearn.document.service.query;

import com.aha.domain.ailearn.document.dto.api.response.UserLearningContentResponseDto;
import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.domain.user.repository.UserExamRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLearningContentQueryService {

    private final UserLearningContentRepository
            userLearningContentRepository;

    private final UserExamRepository userExamRepository;

    private final ObjectMapper objectMapper;

    public UserLearningContentResponseDto getLearningContent(
            Long userId,
            Long userExamId,
            Long examScopeNodeId
    ) {
        validateUserExamOwner(
                userId,
                userExamId
        );

        UserLearningContent learningContent =
                userLearningContentRepository
                        .findByUserExam_IdAndExamScopeNode_Id(
                                userExamId,
                                examScopeNodeId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_LEARNING_CONTENT_NOT_FOUND
                                )
                        );

        return new UserLearningContentResponseDto(
                learningContent.getId(),
                userExamId,
                examScopeNodeId,
                learningContent.getTitle(),
                learningContent.getContent(),
                parseKeywords(
                        learningContent.getKeywordsJson()
                ),
                learningContent.getStatus().name()
        );
    }

    private void validateUserExamOwner(
            Long userId,
            Long userExamId
    ) {
        UserExam userExam =
                userExamRepository.findById(userExamId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_EXAM_NOT_FOUND
                                )
                        );

        if (!userExam.getUser().getId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.USER_EXAM_NOT_FOUND
            );
        }
    }

    private List<String> parseKeywords(
            String keywordsJson
    ) {
        if (keywordsJson == null
                || keywordsJson.isBlank()) {
            return List.of();
        }

        try {
            String[] keywords =
                    objectMapper.readValue(
                            keywordsJson,
                            String[].class
                    );

            return List.of(keywords);

        } catch (Exception exception) {
            return List.of();
        }
    }
}