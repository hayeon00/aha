package com.aha.domain.ailearn.session.dto.response;

import com.aha.domain.ailearn.session.entity.LearningSession;

import java.time.LocalDateTime;

public record LearningSessionCreateResponse(
        Long learningSessionId,
        Long userId,
        Long examScopeNodeId,
        Long learningContentId,
        LocalDateTime startedAt,
        LocalDateTime lastAccessedAt
) {

    public static LearningSessionCreateResponse from(LearningSession session) {
        return new LearningSessionCreateResponse(
                session.getId(),
                session.getUserId(),
                session.getExamScopeNodeId(),
                session.getLearningContentId(),
                session.getStartedAt(),
                session.getLastAccessedAt()
        );
    }
}