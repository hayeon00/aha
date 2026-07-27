package com.aha.domain.notestudio.document.service.processing;

import com.aha.domain.notestudio.document.dto.content.response.DocumentProcessingGroupResponseDto;
import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;
import com.aha.domain.notestudio.document.event.DocumentUploadCompletedEvent;
import com.aha.domain.notestudio.document.repository.DocumentProcessingGroupRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aha.domain.notestudio.document.enums.DocumentProcessingStatus;

@Service
@RequiredArgsConstructor
public class DocumentProcessingRetryService {

    private final DocumentProcessingGroupRepository processingGroupRepository;
    private final DocumentProcessingCleanupService cleanupService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DocumentProcessingGroupResponseDto retry(Long userId, Long processingGroupId) {
        if (userId == null || processingGroupId == null || processingGroupId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        DocumentProcessingGroup processingGroup = processingGroupRepository
                .findOwnedByIdForUpdate(processingGroupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND));

        if (processingGroup.getStatus() != DocumentProcessingStatus.FAILED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        cleanupService.cleanupForRestart(processingGroupId);
        processingGroup.prepareRetry();

        eventPublisher.publishEvent(new DocumentUploadCompletedEvent(processingGroupId));

        return DocumentProcessingGroupResponseDto.from(processingGroup);
    }
}
