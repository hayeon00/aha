package com.aha.domain.notestudio.document.service.upload.model;

import java.util.List;

public record PendingDocumentBatch(Long processingGroupId, List<PendingDocumentUpload> uploads) {

    public PendingDocumentBatch {
        if (processingGroupId == null) {
            throw new IllegalArgumentException("처리 그룹 ID는 필수입니다.");
        }

        if (uploads == null || uploads.isEmpty()) {
            throw new IllegalArgumentException("업로드 문서 목록은 필수입니다.");
        }

        uploads = List.copyOf(uploads);
    }
}