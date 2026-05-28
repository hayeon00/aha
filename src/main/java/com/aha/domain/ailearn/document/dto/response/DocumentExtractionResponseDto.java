package com.aha.domain.ailearn.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentExtractionResponseDto {

    private Long sourceDocumentId;
    private Long processingId;
    private int extractedChunkCount;
    private String status;
}