package com.aha.domain.document.service.processing;

import com.aha.domain.document.dto.response.DocumentProcessingResponseDto;
import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentProcessingQueryService {

    private final DocumentProcessingRepository documentProcessingRepository;

    public DocumentProcessingResponseDto get(Long userId, Long processingId) {
        if (processingId == null || processingId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        DocumentProcessing processing = documentProcessingRepository
                .findOwnedById(processingId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND
                ));

        return DocumentProcessingResponseDto.from(processing);
    }
}
