package com.aha.domain.ailearn.document.dto.response;

import com.aha.domain.ailearn.document.entity.AiGeneratedLearningContentBody;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneratedLearningContentBodyResponseDto {

    private Long generatedBodyId;
    private String bodyType;
    private String title;
    private String content;
    private String reviewStatus;

    public static GeneratedLearningContentBodyResponseDto from(AiGeneratedLearningContentBody body) {
        return new GeneratedLearningContentBodyResponseDto(
                body.getId(),
                body.getBodyType().name(),
                body.getTitle(),
                body.getContent(),
                body.getReviewStatus().name()
        );
    }
}