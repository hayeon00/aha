package com.aha.domain.ailearn.document.service.content;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.DocumentScopeMappingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentLearningContentBatchGenerationService
 * @since : 2026. 6. 25. 목요일
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLearningContentBatchGenerationService {

    private final DocumentProcessingGroupRepository processingGroupRepository;
    private final DocumentScopeMappingRepository  documentScopeMappingRepository;
    private final DocumentBasedLearningContentGenerationService  generationService;

    public void generateLearningContents(Long processingGroupId){

        validateInput(processingGroupId);

        DocumentProcessingGroup processingGroup = processingGroupRepository.findByIdWithUserExamAndUser(processingGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));

        generateLearningContents(processingGroup);

    }

    public void generateLearningContents(DocumentProcessingGroup processingGroup){

        validateProcessingGroup(processingGroup);

        Long processingGroupId = processingGroup.getId();

        Long userExamId = processingGroup.getUserExam().getId();

        Long userId = processingGroup.getUserExam().getUser().getId();

        List<Long> examScopeNodeIds = documentScopeMappingRepository.findDistinctExamScopeNodeIdsByProcessingGroupId(processingGroupId);

        if(examScopeNodeIds.isEmpty()){

            log.info(
                    "개념 설명을 생성할 매핑 목차가 없습니다. userId={}, processingGroupId={}",
                    userId,
                    processingGroupId
            );

            return;
        }

        log.info(
                "목차별 개념 설명 일괄 생성 시작. userId={}, processingGroupId={}, targetCount={}",
                userId,
                processingGroupId,
                examScopeNodeIds.size()
        );

        for(Long examScopeNodeId : examScopeNodeIds){
            generationService.generate(userId, userExamId, examScopeNodeId);
        }

        log.info(
                "목차별 개념 설명 일괄 생성 완료. userId={}, processingGroupId={}, generatedCount={}",
                userId,
                processingGroupId,
                examScopeNodeIds.size()
        );

    }

    private void validateProcessingGroup(DocumentProcessingGroup processingGroup) {

        if(processingGroup.getUserExam() == null || processingGroup.getUserExam().getId() == null || processingGroup.getUserExam().getUser() == null
            || processingGroup.getUserExam().getUser().getId() == null){

            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND);
        }
    }


    private void validateInput(Long processingGroupId) {

        if(processingGroupId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

}
