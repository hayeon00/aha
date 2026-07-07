package com.aha.domain.ailearn.document.dto.upload.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;

import java.util.List;

public record BatchUploadResponseDto(
        Long processingGroupId,
        Long userExamId,
        String status,
        String currentStep,
        Integer progressRate,
        Integer totalFileCount,
        Integer completedFileCount,
        Integer failedFileCount,
        List<UploadedResponseDto> documents
) {

    public static BatchUploadResponseDto of(
            DocumentProcessingGroup processingGroup,
            List<UploadedResponseDto> documents
    ) {
        return new BatchUploadResponseDto(
                processingGroup.getId(),
                processingGroup.getUserExam().getId(),
                processingGroup.getStatus().name(),
                processingGroup.getCurrentStep().name(),
                processingGroup.getProgressRate(),
                processingGroup.getTotalFileCount(),
                processingGroup.getCompletedFileCount(),
                processingGroup.getFailedFileCount(),
                documents
        );
    }
}