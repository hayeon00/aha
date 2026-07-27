package com.aha.domain.notestudio.document.service.content;

import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;
import com.aha.domain.notestudio.document.entity.LearningNote;
import com.aha.domain.notestudio.document.entity.LearningNoteContent;
import com.aha.domain.notestudio.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.notestudio.document.repository.LearningNoteContentRepository;
import com.aha.domain.notestudio.document.repository.LearningNoteRepository;
import com.aha.domain.notestudio.document.service.content.model.GeneratedLearningContent;
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
    private final LearningNoteRepository learningNoteRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;

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

        LearningNote note = learningNoteRepository
                .findByProcessingGroup_Id(processingGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        LearningNoteContent learningContent =
                learningNoteContentRepository
                        .findByLearningNote_IdAndExamScopeNode_Id(
                                note.getId(),
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
                                LearningNoteContent.create(
                                        note,
                                        examScopeNode,
                                        generatedContent.title(),
                                        generatedContent.body()
                                )
                        );

        learningNoteContentRepository.save(
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
