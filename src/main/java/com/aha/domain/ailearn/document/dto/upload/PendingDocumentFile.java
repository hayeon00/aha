package com.aha.domain.ailearn.document.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingDocumentFile {

    private Long sourceDocumentId;
    private String originalFileName;
    private String storageKey;
}