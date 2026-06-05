package com.aha.domain.ailearn.document.dto.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;

public record DocumentProcessingGroupResponseDto(
        Long processingId,
        Long processingGroupId,
        Long userExamId,
        String status,
        String currentStep,
        Integer progressRate,
        Integer totalFileCount,
        Integer completedFileCount,
        Integer failedFileCount,
        String errorMessage
) {

    public static DocumentProcessingGroupResponseDto from(
            DocumentProcessingGroup processingGroup
    ) {
        return new DocumentProcessingGroupResponseDto(
                processingGroup.getId(),
                processingGroup.getId(),
                processingGroup.getUserExam().getId(),
                processingGroup.getStatus().name(),
                processingGroup.getCurrentStep().name(),
                processingGroup.getProgressRate(),
                processingGroup.getTotalFileCount(),
                processingGroup.getCompletedFileCount(),
                processingGroup.getFailedFileCount(),
                processingGroup.getErrorMessage()
        );
    }
}
