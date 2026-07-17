package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.client.content.dto.OpenAiChatRequest;
import com.aha.domain.ailearn.document.client.content.dto.OpenAiChatResponse;
import com.aha.domain.ailearn.document.service.content.model.GeneratedLearningContent;
import com.aha.domain.ailearn.document.service.content.model.TopicDocumentContext;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiLearningContentGenerationClient
        implements LearningContentGenerationClient {

    private static final String MODEL = "gpt-4.1-mini";

    private final RestClient openAiRestClient;
    private final LearningContentPromptBuilder promptBuilder;

    public OpenAiLearningContentGenerationClient(
            @Qualifier("openAiRestClient")
            RestClient openAiRestClient,
            LearningContentPromptBuilder promptBuilder
    ) {
        this.openAiRestClient = openAiRestClient;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public GeneratedLearningContent generate(
            TopicDocumentContext context
    ) {
        try {
            OpenAiChatRequest request =
                    OpenAiChatRequest.of(
                            MODEL,
                            promptBuilder.buildSystemPrompt(),
                            promptBuilder.buildUserPrompt(context)
                    );

            OpenAiChatResponse response =
                    openAiRestClient.post()
                            .uri("/chat/completions")
                            .body(request)
                            .retrieve()
                            .body(OpenAiChatResponse.class);

            String generatedBody =
                    response == null
                            ? null
                            : response.firstContent();

            if (generatedBody == null
                    || generatedBody.isBlank()) {
                throw new BusinessException(
                        ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
                );
            }

            return new GeneratedLearningContent(
                    context.topicTitle(),
                    generatedBody.trim()
            );

        } catch (BusinessException exception) {
            throw exception;

        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }
    }
}