package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.user.entity.UserExam;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : UserLearningContentPersistenceService
 * @since : 2026. 6. 25. 목요일
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLearningContentPersistenceService {
    
    private final UserLearningContentRepository userLearningContentRepository;
    
    @Transactional
    public void saveOrUpdate(UserExam userExam, ExamScopeNode examScopeNode, String generatedContent) {
        
        validateInput(userExam, examScopeNode, generatedContent);

        validateLearningContentTarget(examScopeNode);
        
        userLearningContentRepository.findByUserExam_IdAndExamScopeNode_Id(userExam.getId(), examScopeNode.getId())
                .ifPresentOrElse(existingContent -> updateExistingContent(existingContent, examScopeNode, generatedContent),
                () -> createNewContent(userExam, examScopeNode, generatedContent));
    }

    private void createNewContent(UserExam userExam, ExamScopeNode examScopeNode, String generatedContent) {

        UserLearningContent newContent = UserLearningContent.builder()
                .userExam(userExam)
                .examScopeNode(examScopeNode)
                .title(examScopeNode.getTitle())
                .content(generatedContent)
                .keywordsJson(null)
                .build();

        UserLearningContent savedContent = userLearningContentRepository.save(newContent);

        log.info(
                "사용자 개념 설명 신규 저장 완료. userLearningContentId={}, userExamId={}, examScopeNodeId={}",
                savedContent.getId(),
                userExam.getId(),
                examScopeNode.getId()
        );

    }

    private void updateExistingContent(UserLearningContent existingContent, ExamScopeNode examScopeNode, String generatedContent) {

        existingContent.updateContent(examScopeNode.getTitle(), generatedContent, null);

        log.info(
                "사용자 개념 설명 갱신 완료. userLearningContentId={}, userExamId={}, examScopeNodeId={}",
                existingContent.getId(),
                existingContent.getUserExam().getId(),
                examScopeNode.getId()
        );

    }

    private void validateLearningContentTarget(ExamScopeNode examScopeNode) {

        if(!examScopeNode.isLeaf() || !examScopeNode.isActive()){

            throw new BusinessException(ErrorCode.INVALID_LEARNING_CONTENT_TARGET);
        }
    }

    private void validateInput(UserExam userExam, ExamScopeNode examScopeNode, String generatedContent) {
        
        if(userExam == null || examScopeNode == null || generatedContent == null || generatedContent.isBlank()) {
            
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
