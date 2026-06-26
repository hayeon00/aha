package com.aha.domain.ailearn.document.dto.content.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentBasedLearningContentResponseDto
 * @since : 2026. 6. 25. 목요일
 */

@Getter
@Builder
public class DocumentBasedLearningContentResponseDto {

    private Long examScopeNodeId;
    private String topicTitle;
    private int mappedChunkCount;
    private String content;
}
