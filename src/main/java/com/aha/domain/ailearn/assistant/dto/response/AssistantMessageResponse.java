package com.aha.domain.ailearn.assistant.dto.response;

import com.aha.domain.ailearn.assistant.entity.AiQuestionType;

public record AssistantMessageResponse(
        Long learningSessionId,
        Long examScopeNodeId,
        AiQuestionType questionType,
        String userMessage,
        String assistantMessage
) {
}