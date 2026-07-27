package com.aha.domain.notestudio.document.dto.content.response;

import com.aha.domain.notestudio.document.entity.DocumentProcessingGroup;
import com.aha.domain.notestudio.document.enums.DocumentProcessingStep;
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
    private String stepMessage;
    private int progressRate;
    private int completedFileCount;
    private int totalFileCount;
    private int failedFileCount;
    private String errorMessage;

    public static DocumentProcessingStateResponseDto idle() {
        return DocumentProcessingStateResponseDto.builder()
                .processingGroupId(null)
                .status("IDLE")
                .currentStep(null)
                .stepMessage(null)
                .progressRate(0)
                .completedFileCount(0)
                .totalFileCount(0)
                .errorMessage(null)
                .build();
    }

    public static DocumentProcessingStateResponseDto from(
            DocumentProcessingGroup processingGroup
    ) {
        return DocumentProcessingStateResponseDto.builder()
                .processingGroupId(processingGroup.getId())
                .status(processingGroup.getStatus().name())
                .currentStep(processingGroup.getCurrentStep())
                .stepMessage(processingGroup.getCurrentStep().getMessage())
                .totalFileCount(processingGroup.getTotalFileCount())
                .errorMessage(processingGroup.getErrorMessage())
                .build();
    }
}
