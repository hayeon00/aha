package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.UserLearningContentRepository;
import com.aha.domain.ailearn.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningContentPersistenceService {

    private final DocumentProcessingGroupRepository processingGroupRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final UserLearningContentRepository userLearningContentRepository;

    @Transactional
    public void saveOrReplace(
            Long processingGroupId,
            Long examScopeNodeId,
            GeneratedLearningContent generatedContent
    ) {
        validateArguments(
                processingGroupId,
                examScopeNodeId,
                generatedContent
        );

        DocumentProcessingGroup processingGroup =
                processingGroupRepository
                        .findById(processingGroupId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND
                                )
                        );

        ExamScopeNode examScopeNode =
                examScopeNodeRepository
                        .findById(examScopeNodeId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND
                                )
                        );

        Long userExamId = processingGroup
                .getUserExam()
                .getId();

        UserLearningContent learningContent =
                userLearningContentRepository
                        .findByUserExam_IdAndExamScopeNode_Id(
                                userExamId,
                                examScopeNodeId
                        )
                        .map(existingContent -> {
                            existingContent.replace(
                                    generatedContent.title(),
                                    generatedContent.body()
                            );

                            return existingContent;
                        })
                        .orElseGet(() ->
                                UserLearningContent.create(
                                        processingGroup.getUserExam(),
                                        examScopeNode,
                                        generatedContent.title(),
                                        generatedContent.body()
                                )
                        );

        userLearningContentRepository.save(
                learningContent
        );
    }

    private void validateArguments(
            Long processingGroupId,
            Long examScopeNodeId,
            GeneratedLearningContent generatedContent
    ) {
        if (processingGroupId == null
                || processingGroupId <= 0
                || examScopeNodeId == null
                || examScopeNodeId <= 0
                || generatedContent == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }
}