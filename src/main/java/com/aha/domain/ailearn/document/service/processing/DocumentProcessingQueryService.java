package com.aha.domain.ailearn.document.service.processing;

import com.aha.domain.ailearn.document.dto.content.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.ailearn.document.dto.content.response.DocumentProcessingStateResponseDto;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentProcessingQueryService
 * @since : 2026. 6. 25. 목요일
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentProcessingQueryService {

    private final DocumentProcessingGroupRepository  documentProcessingGroupRepository;

    public DocumentProcessingGroupResponseDto getProcessingGroup(Long userId, Long processingGroupId) {

        validateInput(userId, processingGroupId);

        DocumentProcessingGroup processingGroup = documentProcessingGroupRepository.findByIdAndUserExam_User_Id(processingGroupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));

        return DocumentProcessingGroupResponseDto.from(processingGroup);
    }

    public DocumentProcessingStateResponseDto getLatestProcessingState(Long userId, Long userExamId){
        return documentProcessingGroupRepository.findTopByUserExam_IdAndUserExam_User_IdOrderByCreatedAtDesc(userExamId, userId)
                .map(DocumentProcessingStateResponseDto::from)
                .orElseGet(DocumentProcessingStateResponseDto::idle);
    }

    private void validateInput(Long userId, Long processingGroupId) {

        if(userId==null || processingGroupId==null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
