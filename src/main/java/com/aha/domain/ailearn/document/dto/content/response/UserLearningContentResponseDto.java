package com.aha.domain.ailearn.document.dto.content.response;

import com.aha.domain.ailearn.document.entity.UserLearningContent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : UserLearningContentResponseDto
 * @since : 2026. 6. 25. 목요일
 */

@Getter
@Builder
public class UserLearningContentResponseDto {

    private Long userLearningContentId;
    private Long userExamId;
    private Long examScopeNodeId;

    private String title;
    private String content;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserLearningContentResponseDto from(UserLearningContent learningContent) {

        return UserLearningContentResponseDto.builder()
                .userLearningContentId(learningContent.getId())
                .userExamId(learningContent.getUserExam().getId())
                .examScopeNodeId(learningContent.getExamScopeNode().getId())
                .title(learningContent.getTitle())
                .content(learningContent.getContent())
                .status(learningContent.getStatus().name())
                .createdAt(learningContent.getCreatedAt())
                .updatedAt(learningContent.getUpdatedAt())
                .build();
    }
}
