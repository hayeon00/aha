package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentProcessingStateResponseDto
 * @since : 2026. 7. 2. 목요일
 */
@Getter
@Builder
public class DocumentProcessingStateResponseDto {

    private Long processingGroupId;
    private String status;
    private DocumentProcessingStep currentStep;
    private int progressRate;
    private int completedFileCount;
    private int totalFileCount;
    private int failedFileCount;

    public static DocumentProcessingStateResponseDto idle() {
        return DocumentProcessingStateResponseDto.builder()
                .processingGroupId(null)
                .status("IDLE")
                .currentStep(null)
                .progressRate(0)
                .completedFileCount(0)
                .totalFileCount(0)
                .failedFileCount(0)
                .build();
    }

    public static DocumentProcessingStateResponseDto from(
            DocumentProcessingGroup processingGroup
    ) {
        return DocumentProcessingStateResponseDto.builder()
                .processingGroupId(processingGroup.getId())
                .status(processingGroup.getStatus().name())
                .currentStep(processingGroup.getCurrentStep())
                .progressRate(processingGroup.getProgressRate())
                .completedFileCount(processingGroup.getCompletedFileCount())
                .totalFileCount(processingGroup.getTotalFileCount())
                .failedFileCount(processingGroup.getFailedFileCount())
                .build();
    }
}