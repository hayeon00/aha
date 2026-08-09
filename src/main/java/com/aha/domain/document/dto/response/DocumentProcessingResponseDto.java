package com.aha.domain.document.dto.response;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.enums.DocumentProcessingStatus;
import com.aha.domain.document.enums.DocumentProcessingStep;

import java.time.LocalDateTime;

public record DocumentProcessingResponseDto(
        Long processingId,
        Long learningNoteId,
        DocumentProcessingStatus status,
        DocumentProcessingStep currentStep,
        String errorCode,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {

    public static DocumentProcessingResponseDto from(DocumentProcessing processing) {
        return new DocumentProcessingResponseDto(
                processing.getId(),
                processing.getLearningNote().getId(),
                processing.getStatus(),
                processing.getCurrentStep(),
                processing.getErrorCode(),
                processing.getErrorMessage(),
                processing.getStartedAt(),
                processing.getCompletedAt()
        );
    }
}
