package com.aha.domain.ailearn.document.service.generation;

import com.aha.domain.ailearn.document.dto.generation.response.LearningContentGenerationResultDto;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.user.entity.UserExam;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLearningContentPersistenceService {

    private final UserLearningContentRepository userLearningContentRepository;
    private final DocumentProcessingGroupRepository documentProcessingGroupRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;

    private final ObjectMapper objectMapper;

    /**
     * AI가 생성한 목차별 개념 설명을 저장한다.
     *
     * 같은 사용자 시험과 같은 목차에 기존 콘텐츠가 있으면 수정하고,
     * 없으면 새로 생성한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdate(
            Long processingGroupId,
            Long examScopeNodeId,
            LearningContentGenerationResultDto result
    ) {
        validateRequest(
                processingGroupId,
                examScopeNodeId,
                result
        );

        DocumentProcessingGroup processingGroup =
                getProcessingGroup(
                        processingGroupId
                );

        UserExam userExam =
                processingGroup.getUserExam();

        ExamScopeNode scopeNode =
                getExamScopeNode(
                        examScopeNodeId
                );

        validateSameExamVersion(
                userExam,
                scopeNode
        );

        String keywordsJson =
                convertKeywordsToJson(
                        result
                );

        try {
            userLearningContentRepository
                    .findByUserExam_IdAndExamScopeNode_Id(
                            userExam.getId(),
                            scopeNode.getId()
                    )
                    .ifPresentOrElse(
                            existingContent ->
                                    existingContent.updateContent(
                                            result.title(),
                                            result.content(),
                                            keywordsJson
                                    ),
                            () ->
                                    userLearningContentRepository.save(
                                            UserLearningContent.builder()
                                                    .userExam(userExam)
                                                    .examScopeNode(scopeNode)
                                                    .title(result.title())
                                                    .content(result.content())
                                                    .keywordsJson(keywordsJson)
                                                    .build()
                                    )
                    );

            log.info(
                    "사용자 개념 설명 저장 완료. processingGroupId={}, userExamId={}, examScopeNodeId={}",
                    processingGroupId,
                    userExam.getId(),
                    scopeNode.getId()
            );

        } catch (BusinessException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "사용자 개념 설명 저장 실패. processingGroupId={}, userExamId={}, examScopeNodeId={}",
                    processingGroupId,
                    userExam.getId(),
                    scopeNode.getId(),
                    exception
            );

            throw new BusinessException(
                    ErrorCode.USER_LEARNING_CONTENT_SAVE_FAILED
            );
        }
    }

    private DocumentProcessingGroup getProcessingGroup(
            Long processingGroupId
    ) {
        return documentProcessingGroupRepository
                .findById(processingGroupId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND
                        )
                );
    }

    private ExamScopeNode getExamScopeNode(
            Long examScopeNodeId
    ) {
        return examScopeNodeRepository
                .findById(examScopeNodeId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND
                        )
                );
    }

    /**
     * 다른 시험 버전의 목차가 잘못 저장되는 것을 방지한다.
     */
    private void validateSameExamVersion(
            UserExam userExam,
            ExamScopeNode scopeNode
    ) {
        Long userExamVersionId =
                userExam.getExamVersion().getId();

        Long scopeExamVersionId =
                scopeNode.getExamVersion().getId();

        if (!userExamVersionId.equals(
                scopeExamVersionId
        )) {
            log.error(
                    "사용자 시험 버전과 목차 시험 버전이 일치하지 않습니다. userExamVersionId={}, scopeExamVersionId={}",
                    userExamVersionId,
                    scopeExamVersionId
            );

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private String convertKeywordsToJson(
            LearningContentGenerationResultDto result
    ) {
        if (result.keywords() == null
                || result.keywords().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(
                    result.keywords()
            );

        } catch (Exception exception) {
            log.error(
                    "개념 설명 키워드 JSON 변환 실패. keywords={}",
                    result.keywords(),
                    exception
            );

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }

    private void validateRequest(
            Long processingGroupId,
            Long examScopeNodeId,
            LearningContentGenerationResultDto result
    ) {
        if (processingGroupId == null
                || examScopeNodeId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (result == null
                || result.title() == null
                || result.title().isBlank()
                || result.content() == null
                || result.content().isBlank()) {

            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }
    }
}