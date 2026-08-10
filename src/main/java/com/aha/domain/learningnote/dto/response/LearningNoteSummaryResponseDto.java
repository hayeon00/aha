package com.aha.domain.learningnote.dto.response;

import java.time.LocalDateTime;

public record LearningNoteSummaryResponseDto(
        Long id,
        String title,
        String status,
        Long documentId,
        Long tocId,
        Long userExamId,
        String examCode,
        String examName,
        int sourceDocumentCount,
        LocalDateTime updatedAt
) {
}
