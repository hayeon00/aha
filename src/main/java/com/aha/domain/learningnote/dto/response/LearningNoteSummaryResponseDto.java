package com.aha.domain.learningnote.dto.response;

import java.time.LocalDateTime;

public record LearningNoteSummaryResponseDto(
        Long id,
        String title,
        String status,
        Long documentId,
        Long tocId,
        int sourceDocumentCount,
        LocalDateTime updatedAt
) {
}
