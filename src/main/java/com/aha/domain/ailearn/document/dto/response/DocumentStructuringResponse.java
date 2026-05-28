package com.aha.domain.ailearn.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentStructuringResponse {

    private Long sourceDocumentId;
    private Long processingId;
    private int generatedBodyCount;
    private String status;
}