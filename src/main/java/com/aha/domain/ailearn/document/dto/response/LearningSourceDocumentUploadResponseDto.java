package com.aha.domain.ailearn.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningSourceDocumentUploadResponseDto {

    private Long sourceDocumentId;
    private Long processingId;
    private String status;
}