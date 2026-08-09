package com.aha.domain.learningnote.client.generation;

import com.aha.domain.document.client.mapping.dto.OpenAiChatCompletionResponse;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiConceptExplanationClient implements ConceptExplanationClient {

    private static final int MAX_ATTEMPTS = 2;

    private final RestClient openAiRestClient;
    private final ConceptExplanationPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiConceptExplanationClient(
            @Qualifier("openAiRestClient") RestClient openAiRestClient,
            ConceptExplanationPromptBuilder promptBuilder,
            ObjectMapper objectMapper,
            @Value("${openAI.model:gpt-4.1-mini}") String model
    ) {
        this.openAiRestClient = openAiRestClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public ConceptExplanationResult generate(ConceptExplanationRequest request) {
        String prompt = promptBuilder.build(request);
        BusinessException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                ConceptExplanationResult result = parse(requestOpenAi(prompt));
                log.info("목차별 개념 설명 생성 완료. examScopeNodeId={}, attempt={}",
                        request.examScopeNodeId(), attempt);
                return result;
            } catch (BusinessException exception) {
                lastException = exception;
                if (attempt < MAX_ATTEMPTS) {
                    log.warn("목차별 개념 설명 생성 재시도. examScopeNodeId={}, attempt={}/{}",
                            request.examScopeNodeId(), attempt, MAX_ATTEMPTS);
                }
            }
        }

        log.error("목차별 개념 설명 생성 최종 실패. examScopeNodeId={}",
                request.examScopeNodeId(), lastException);
        throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
    }

    private String requestOpenAi(String prompt) {
        try {
            OpenAiChatCompletionResponse response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(buildRequestBody(prompt))
                    .retrieve()
                    .body(OpenAiChatCompletionResponse.class);

            String content = response == null ? null : response.getFirstContent();
            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
            }
            return content.trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            log.error("OpenAI 개념 설명 생성 HTTP 요청 실패.", exception);
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        } catch (RuntimeException exception) {
            log.error("OpenAI 개념 설명 생성 요청 처리 실패.", exception);
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }

    private ConceptExplanationResult parse(String response) {
        try {
            ConceptExplanationResult result = objectMapper.readValue(
                    normalizeResponse(response), ConceptExplanationResult.class);
            if (result == null
                    || result.title() == null || result.title().isBlank()
                    || result.title().trim().length() > 255
                    || result.content() == null || result.content().isBlank()) {
                throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
            }
            return new ConceptExplanationResult(result.title().trim(), result.content().trim());
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("AI 개념 설명 응답 파싱 실패.", exception);
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }

    private String normalizeResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
        String normalized = response.trim();
        if (normalized.startsWith("```json")) normalized = normalized.substring(7);
        else if (normalized.startsWith("```")) normalized = normalized.substring(3);
        if (normalized.endsWith("```")) normalized = normalized.substring(0, normalized.length() - 3);
        return normalized.trim();
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
                Map.of("role", "system", "content",
                        "당신은 제공된 학습 자료만 근거로 자격증 개념 설명을 만드는 시스템입니다."),
                Map.of("role", "user", "content", prompt)
        ));
        request.put("temperature", 0.2);
        request.put("response_format", buildResponseFormat());
        return request;
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("title", Map.of("type", "string"));
        properties.put("content", Map.of("type", "string"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.of("title", "content"));
        schema.put("additionalProperties", false);

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "concept_explanation",
                        "strict", true,
                        "schema", schema
                )
        );
    }
}
