package com.aha.domain.document.service.processing;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.domain.document.service.processing.model.DocumentProcessingContext;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentProcessingContextService {

    private final DocumentProcessingRepository documentProcessingRepository;

    public DocumentProcessingContext get(Long processingId) {

        validateProcessingId(processingId);

        DocumentProcessing processing = documentProcessingRepository
                .findByIdWithLearningNoteAndSourceDocument(processingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND));

        return new DocumentProcessingContext(
                processing.getId(),
                processing.getLearningNote().getId(),
                processing.getLearningNote().getSourceDocument().getId()
        );
    }

    private void validateProcessingId(Long processingId) {
        if (processingId == null || processingId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
