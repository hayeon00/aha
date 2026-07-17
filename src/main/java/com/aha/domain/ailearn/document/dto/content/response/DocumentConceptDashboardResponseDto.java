package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.enums.UserDocumentConceptSourceType;

import java.util.List;

public record DocumentConceptDashboardResponseDto(
        Long documentId,
        String documentName,
        List<TocConceptItem> mapped,
        List<TocConceptItem> unmapped
) {
    public record TocConceptItem(
            Long tocId,
            String tocTitle,
            int standardOrder,
            boolean generated,
            UserDocumentConceptSourceType sourceType,
            String content,
            UserDocumentConceptResponseDto concept
    ) {}
}
