package com.aha.domain.notestudio.document.client.content.dto;

import java.util.List;

public record OpenAiChatRequest(
        String model,
        List<Message> messages,
        Double temperature
) {

    public record Message(
            String role,
            String content
    ) {
    }

    public static OpenAiChatRequest of(
            String model,
            String systemPrompt,
            String userPrompt
    ) {
        return new OpenAiChatRequest(
                model,
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user", userPrompt)
                ),
                0.2
        );
    }
}