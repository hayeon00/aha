package com.aha.domain.notestudio.document.client.content.dto;

import java.util.List;

public record OpenAiChatResponse(
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

    public String firstContent() {
        if (choices == null
                || choices.isEmpty()
                || choices.get(0).message() == null) {
            return null;
        }

        return choices.get(0)
                .message()
                .content();
    }
}