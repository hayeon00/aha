package com.aha.domain.ailearn.document.service.processing;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import com.aha.domain.ailearn.document.repository.DocumentProcessingGroupRepository;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final String DEFAULT_ERROR_MESSAGE = "문서 처리 중 오류가 발생했습니다.";

    private final DocumentProcessingGroupRepository processingGroupRepository;
    private final DocumentProcessingRepository processingRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean start(Long processingGroupId) {
        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        if (processingGroup.getStatus() != DocumentProcessingStatus.PENDING) {
            log.warn(
                    "문서 처리 시작 요청을 건너뜁니다. processingGroupId={}, status={}",
                    processingGroupId,
                    processingGroup.getStatus()
            );
            return false;
        }

        List<DocumentProcessing> processings =
                getDocumentProcessings(processingGroupId);

        processingGroup.startProcessing();

        for (DocumentProcessing processing : processings) {
            if (processing.getStatus() == DocumentProcessingStatus.PENDING) {
                processing.start();
            }
        }

        log.info(
                "문서 처리 시작 상태 반영 완료. processingGroupId={}, status={}, step={}, fileCount={}",
                processingGroupId,
                processingGroup.getStatus(),
                processingGroup.getCurrentStep(),
                processings.size()
        );

        return true;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStep(
            Long processingGroupId,
            DocumentProcessingStep step
    ) {
        validateStep(step);

        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        processingGroup.updateStep(step);

        log.info(
                "문서 처리 단계 변경. processingGroupId={}, step={}",
                processingGroupId,
                step
        );
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long processingGroupId) {
        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        List<DocumentProcessing> processings =
                getDocumentProcessings(processingGroupId);

        completeProcessingDocuments(processings);

        int completedFileCount = countByStatus(
                processings,
                DocumentProcessingStatus.COMPLETED
        );

        int failedFileCount = countByStatus(
                processings,
                DocumentProcessingStatus.FAILED
        );

        validateAllDocumentsFinished(
                processings.size(),
                completedFileCount,
                failedFileCount
        );

        processingGroup.finish(
                completedFileCount,
                failedFileCount
        );

        log.info(
                "문서 처리 최종 상태 반영 완료. processingGroupId={}, status={}, completedCount={}, failedCount={}",
                processingGroupId,
                processingGroup.getStatus(),
                completedFileCount,
                failedFileCount
        );
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(
            Long processingGroupId,
            String errorMessage
    ) {
        String resolvedErrorMessage =
                resolveErrorMessage(errorMessage);

        DocumentProcessingGroup processingGroup =
                getProcessingGroup(processingGroupId);

        List<DocumentProcessing> processings =
                getDocumentProcessings(processingGroupId);

        for (DocumentProcessing processing : processings) {
            if (!isFinished(processing.getStatus())) {
                processing.fail(resolvedErrorMessage);
            }
        }

        processingGroup.fail(resolvedErrorMessage);

        log.warn(
                "문서 처리 실패 상태 반영 완료. processingGroupId={}, errorMessage={}",
                processingGroupId,
                resolvedErrorMessage
        );
    }

    // ==================== Status Handling ====================

    private void completeProcessingDocuments(
            List<DocumentProcessing> processings
    ) {
        for (DocumentProcessing processing : processings) {
            if (processing.getStatus() == DocumentProcessingStatus.PROCESSING) {
                processing.complete();
            }
        }
    }

    private int countByStatus(
            List<DocumentProcessing> processings,
            DocumentProcessingStatus status
    ) {
        return (int) processings.stream()
                .filter(processing -> processing.getStatus() == status)
                .count();
    }

    private boolean isFinished(DocumentProcessingStatus status) {
        return status == DocumentProcessingStatus.COMPLETED
                || status == DocumentProcessingStatus.FAILED;
    }

    private void validateAllDocumentsFinished(
            int totalFileCount,
            int completedFileCount,
            int failedFileCount
    ) {
        if (completedFileCount + failedFileCount != totalFileCount) {
            throw new BusinessException(
                    ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS
            );
        }
    }

    // ==================== Validation ====================

    private void validateStep(DocumentProcessingStep step) {
        if (step == null) {
            throw new IllegalArgumentException(
                    "문서 처리 단계는 필수입니다."
            );
        }
    }

    // ==================== Repository Access ====================

    private DocumentProcessingGroup getProcessingGroup(
            Long processingGroupId
    ) {
        return processingGroupRepository.findById(processingGroupId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DOCUMENT_PROCESSING_GROUP_NOT_FOUND
                ));
    }

    private List<DocumentProcessing> getDocumentProcessings(
            Long processingGroupId
    ) {
        List<DocumentProcessing> processings =
                processingRepository.findAllByProcessingGroup_IdOrderByIdAsc(
                        processingGroupId
                );

        if (processings.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND
            );
        }

        return processings;
    }

    // ==================== Error Message ====================

    private String resolveErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return DEFAULT_ERROR_MESSAGE;
        }

        String trimmedMessage = errorMessage.trim();

        if (trimmedMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
            return trimmedMessage.substring(
                    0,
                    MAX_ERROR_MESSAGE_LENGTH
            );
        }

        return trimmedMessage;
    }
}
