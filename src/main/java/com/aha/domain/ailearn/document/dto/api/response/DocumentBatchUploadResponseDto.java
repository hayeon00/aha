package com.aha.domain.ailearn.document.dto.api.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;

import java.util.List;

public record DocumentBatchUploadResponseDto(
        Long processingId,
        Long processingGroupId,
        Long userExamId,
        String status,
        String currentStep,
        Integer progressRate,
        Integer totalFileCount,
        Integer completedFileCount,
        Integer failedFileCount,
        List<UploadedDocumentResponseDto> documents
) {

    public static DocumentBatchUploadResponseDto of(
            DocumentProcessingGroup processingGroup,
            List<UploadedDocumentResponseDto> documents
    ) {
        return new DocumentBatchUploadResponseDto(
                processingGroup.getId(),
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
