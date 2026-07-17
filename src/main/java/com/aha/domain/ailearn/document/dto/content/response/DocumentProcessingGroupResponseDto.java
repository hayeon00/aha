package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentProcessingGroupResponseDto
 * @since : 2026. 6. 25. 목요일
 */

@Getter
@Builder
public class DocumentProcessingGroupResponseDto {

    private Long processingGroupId;
    private Long userExamId;

    private String status;
    private String currentStep;
    private String stepMessage;

    private int totalFileCount;

    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentProcessingGroupResponseDto from(DocumentProcessingGroup processingGroup) {

        return DocumentProcessingGroupResponseDto.builder()
                .processingGroupId(processingGroup.getId())
                .userExamId(processingGroup.getUserExam().getId())
                .status(processingGroup.getStatus().name())
                .currentStep(processingGroup.getCurrentStep().name())
                .stepMessage(processingGroup.getCurrentStep().getMessage())
                .totalFileCount(processingGroup.getTotalFileCount())
                .errorMessage(processingGroup.getErrorMessage())
                .startedAt(processingGroup.getStartedAt())
                .completedAt(processingGroup.getCompletedAt())
                .createdAt(processingGroup.getCreatedAt())
                .updatedAt(processingGroup.getUpdatedAt())
                .build();
    }
}
