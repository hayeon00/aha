package com.aha.domain.ailearn.session.dto.request;

import jakarta.validation.constraints.NotNull;

public record LearningSessionCreateRequest(

        @NotNull(message = "목차 ID는 필수입니다.")
        Long examScopeNodeId,

        Long learningContentId
) {
}