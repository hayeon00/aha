package com.aha.domain.ailearn.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningContentPublishResponseDto {

    private Long sourceDocumentId;
    private Long learningContentId;
    private int publishedBodyCount;
}