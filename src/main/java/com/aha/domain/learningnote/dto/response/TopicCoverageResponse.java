package com.aha.domain.learningnote.dto.response;

import com.aha.domain.learningnote.enums.TopicCoverageStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : TopicCoverageResponse
 * @since : 2026. 8. 16. 일요일
 */
@Getter
@Builder
public class TopicCoverageResponse {

    private Long scopeNodeId;

    private String code;

    private String title;

    private TopicCoverageStatus status;

    private int mappedChunkCount;
}