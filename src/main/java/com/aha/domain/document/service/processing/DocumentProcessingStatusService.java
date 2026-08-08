package com.aha.domain.document.service.processing;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.enums.DocumentProcessingStep;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentProcessingStatusService {

    private final DocumentProcessingRepository documentProcessingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(Long processingId) {
        DocumentProcessing processing = getDocumentProcessingForUpdate(processingId);
        processing.start();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeStep(
            Long processingId,
            DocumentProcessingStep nextStep
    ) {
        if (nextStep == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        DocumentProcessing processing = getDocumentProcessingForUpdate(processingId);
        processing.changeStep(nextStep);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long processingId) {
        DocumentProcessing processing = getDocumentProcessingForUpdate(processingId);
        processing.complete();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long processingId, Exception exception) {
        DocumentProcessing processing = getDocumentProcessingForUpdate(processingId);

        String errorCode = (exception instanceof BusinessException be)
                ? be.getErrorCode().name()
                : "INTERNAL_SERVER_ERROR";

        String errorMessage = (exception != null) ? exception.getMessage() : null;

        processing.fail(errorCode, errorMessage);

    }

    private DocumentProcessing getDocumentProcessingForUpdate(Long processingId) {
        if (processingId == null || processingId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return documentProcessingRepository.findByIdForUpdate(processingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND));
    }
}
