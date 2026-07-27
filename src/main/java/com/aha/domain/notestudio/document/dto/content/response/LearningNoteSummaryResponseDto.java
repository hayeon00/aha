package com.aha.domain.notestudio.document.dto.content.response;

import com.aha.domain.notestudio.document.entity.LearningNote;

import java.time.LocalDateTime;

public record LearningNoteSummaryResponseDto(
        Long id,
        String title,
        String status,
        Long processingGroupId,
        Long documentId,
        Long tocId,
        int sourceDocumentCount,
        LocalDateTime updatedAt
) {
}
