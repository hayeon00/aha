package com.aha.domain.learningnote.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Builder
public class LearningNoteCoverageResponse {

    private Long learningNoteId;

    private int totalTopicCount;

    private int coveredTopicCount;

    private BigDecimal coverageRate;

    private List<TopicCoverageResponse> topics;
}