package com.aha.domain.learningnote.dto.response;

import com.aha.domain.learningnote.enums.LearningContentSourceType;

import java.time.LocalDateTime;
import java.util.List;


public record LearningNoteDetailResponseDto(
        Long id,
        String title,
        Long userExamId,
        String examName,
        List<DocumentItem> documents,
        List<ContentItem> contents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt
) {

    public LearningNoteDetailResponseDto {
        documents = documents == null ? List.of() : List.copyOf(documents);
        contents = contents == null ? List.of() : List.copyOf(contents);
    }

    public record DocumentItem(
            Long documentId,
            String fileName,
            String fileExtension,
            long fileSize
    ) {
    }

    public record ContentItem(
            Long contentId,
            Long tocId,
            String tocTitle,
            int displayOrder,
            LearningContentSourceType sourceType,
            String content
    ) {
    }
}
