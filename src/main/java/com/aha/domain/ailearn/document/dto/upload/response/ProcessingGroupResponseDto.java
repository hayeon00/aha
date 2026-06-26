package com.aha.domain.ailearn.document.dto.upload.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;

public record ProcessingGroupResponseDto(
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

    public static ProcessingGroupResponseDto from(
            DocumentProcessingGroup processingGroup
    ) {
        return new ProcessingGroupResponseDto(
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
