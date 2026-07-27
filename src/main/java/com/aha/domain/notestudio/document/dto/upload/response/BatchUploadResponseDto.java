package com.aha.domain.notestudio.document.dto.upload.response;

import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;

import java.util.List;

public record BatchUploadResponseDto(
        Long processingGroupId,
        Long userExamId,
        String status,
        String currentStep,
        String stepMessage,
        Integer totalFileCount,
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
                processingGroup.getCurrentStep().getMessage(),
                processingGroup.getTotalFileCount(),
                documents
        );
    }
}
