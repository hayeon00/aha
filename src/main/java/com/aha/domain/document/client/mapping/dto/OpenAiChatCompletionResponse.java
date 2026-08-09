package com.aha.domain.document.client.mapping.dto;

import java.util.List;

public record OpenAiChatCompletionResponse(
        List<Choice> choices
) {

    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }

    /**
     * 첫 번째 AI 응답 메시지의 content를 반환한다.
     */
    public String getFirstContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }

        Choice firstChoice = choices.get(0);

        if (firstChoice == null
                || firstChoice.message() == null) {
            return null;
        }

        return firstChoice.message().content();
    }
}