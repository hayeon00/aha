package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.client.content.dto.OpenAiChatRequest;
import com.aha.domain.ailearn.document.client.content.dto.OpenAiChatResponse;
import com.aha.domain.ailearn.document.service.content.model.GeneratedLearningContent;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiConceptGenerationClient {

    private static final String MODEL = "gpt-4.1-mini";
    private final RestClient openAiRestClient;

    public AiConceptGenerationClient(
            @Qualifier("openAiRestClient") RestClient openAiRestClient
    ) {
        this.openAiRestClient = openAiRestClient;
    }

    public GeneratedLearningContent generate(String title, String parentContext) {
        String systemPrompt = """
                당신은 자격증 시험 학습을 돕는 전문 교육 콘텐츠 작성자입니다.
                아래 목차의 표준 개념 설명을 정확하고 간결한 한국어 마크다운으로 작성하세요.
                특정 업로드 문서를 인용하거나 문서에 근거한 것처럼 표현하지 마세요.
                핵심 정의, 주요 원리, 시험에서 확인할 포인트, 짧은 예시 순으로 구성하세요.
                불확실하거나 시험별로 달라질 수 있는 수치·규정은 단정하지 마세요.
                제목은 본문에 반복하지 말고 본문만 출력하세요.
                """;
        String userPrompt = """
                [상위 목차 맥락]
                %s

                [설명할 목차]
                %s
                """.formatted(parentContext, title);

        try {
            OpenAiChatResponse response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(OpenAiChatRequest.of(MODEL, systemPrompt, userPrompt))
                    .retrieve()
                    .body(OpenAiChatResponse.class);
            String body = response == null ? null : response.firstContent();
            if (body == null || body.isBlank()) {
                throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
            }
            return new GeneratedLearningContent(title, body);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }
}
