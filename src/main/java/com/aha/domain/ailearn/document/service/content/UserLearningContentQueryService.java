package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.dto.content.response.DocumentProcessingStateResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.UserLearningContentResponseDto;
import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : UserLearningContentQueryService
 * @since : 2026. 6. 25. 목요일
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLearningContentQueryService {

    private final UserLearningContentRepository userLearningContentRepository;

    public UserLearningContentResponseDto getLearningContent(Long userId, Long userExamId, Long examScopeNodeId){
        if(userId==null || userExamId == null || examScopeNodeId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UserLearningContent learningContent = userLearningContentRepository.findByUserIdAndUserExamIdAndExamScopeNodeId(
                userId, userExamId, examScopeNodeId).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        return UserLearningContentResponseDto.from(learningContent);
    }

}
