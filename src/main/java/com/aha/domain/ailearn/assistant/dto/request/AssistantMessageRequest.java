package com.aha.domain.ailearn.assistant.dto.request;

import com.aha.domain.ailearn.assistant.entity.AiQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssistantMessageRequest(

        @NotNull(message = "질문 유형은 필수입니다.")
        AiQuestionType questionType,

        @NotBlank(message = "질문 내용은 필수입니다.")
        String message

) {
}