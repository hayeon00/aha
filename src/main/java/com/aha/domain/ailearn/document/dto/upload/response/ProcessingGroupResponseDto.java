package com.aha.domain.ailearn.document.dto.upload.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;

public record ProcessingGroupResponseDto(
        Long processingGroupId,
        Long userExamId,
        String status,
        String currentStep,
        Integer totalFileCount,
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
                processingGroup.getTotalFileCount(),
                processingGroup.getErrorMessage()
        );
    }
}
