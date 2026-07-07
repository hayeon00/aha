package com.aha.domain.ailearn.document.service.upload.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PendingDocumentBatch {

    private Long processingGroupId;
    private List<PendingDocumentFile> documents;
}