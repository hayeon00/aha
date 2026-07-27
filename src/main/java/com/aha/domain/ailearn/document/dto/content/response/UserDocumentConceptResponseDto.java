package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.UserDocumentConcept;
import com.aha.domain.ailearn.document.enums.UserDocumentConceptSourceType;

import java.time.LocalDateTime;

public record UserDocumentConceptResponseDto(
        Long conceptId,
        Long documentId,
        Long tocId,
        String tocTitle,
        String title,
        String content,
        UserDocumentConceptSourceType sourceType,
        boolean isExternalKnowledge,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserDocumentConceptResponseDto from(UserDocumentConcept concept) {
        return new UserDocumentConceptResponseDto(
                concept.getId(), concept.getDocument().getId(), concept.getToc().getId(),
                concept.getToc().getTitle(), concept.getTitle(), concept.getContent(),
                concept.getSourceType(),
                concept.getSourceType() == UserDocumentConceptSourceType.AI_GENERATED,
                concept.getCreatedAt(), concept.getUpdatedAt());
    }
}
