package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.UserLearningContent;
import com.aha.domain.ailearn.document.enums.LearningContentSourceType;

import java.time.LocalDateTime;

public record UserLearningContentResponseDto(
        Long learningContentId,
        Long topicId,
        String topicTitle,
        String title,
        String content,
        LearningContentSourceType sourceType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UserLearningContentResponseDto from(
            UserLearningContent learningContent
    ) {
        return new UserLearningContentResponseDto(
                learningContent.getId(),
                learningContent.getExamScopeNode().getId(),
                learningContent.getExamScopeNode().getTitle(),
                learningContent.getTitle(),
                learningContent.getContent(),
                learningContent.getSourceType(),
                learningContent.getCreatedAt(),
                learningContent.getUpdatedAt()
        );
    }

}
