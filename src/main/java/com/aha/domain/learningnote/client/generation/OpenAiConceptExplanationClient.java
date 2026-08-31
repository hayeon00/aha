package com.aha.domain.learningnote.client.generation;

import com.aha.domain.document.client.mapping.dto.OpenAiChatCompletionResponse;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationResult;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationBatchItem;
import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationBatchResponse;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.openai.OpenAiApiExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class OpenAiConceptExplanationClient implements ConceptExplanationClient {

    private static final long MAX_RETRY_BACKOFF_MILLIS = 10_000L;

    private final RestClient openAiRestClient;
    private final ConceptExplanationPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxAttempts;
    private final long retryBackoffMillis;
    private final int maxOutputTokens;

    public OpenAiConceptExplanationClient(
            @Qualifier("openAiRestClient") RestClient openAiRestClient,
            ConceptExplanationPromptBuilder promptBuilder,
            ObjectMapper objectMapper,
            @Value("${openAI.model:gpt-4.1-mini}") String model,
            @Value("${document.processing.retry-max-attempts:3}") int maxAttempts,
            @Value("${document.processing.retry-backoff-millis:1000}") long retryBackoffMillis,
            @Value("${document.processing.content-generation-max-output-tokens:1000}")
            int maxOutputTokens
    ) {
        this.openAiRestClient = openAiRestClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryBackoffMillis = Math.max(0L, retryBackoffMillis);
        this.maxOutputTokens = Math.max(256, maxOutputTokens);
    }

    @Override
    public ConceptExplanationResult generate(ConceptExplanationRequest request) {
        String prompt = promptBuilder.build(request);
        BusinessException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long attemptStartedAt = System.nanoTime();
            try {
                ConceptExplanationResult result = parse(
                        requestOpenAi(prompt, buildResponseFormat())
                );
                log.info(
                        "[DOCUMENT_PERF] content generation AI request completed. examScopeNodeId={}, attempt={}, elapsedMs={}, thread={}",
                        request.examScopeNodeId(),
                        attempt,
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - attemptStartedAt),
                        Thread.currentThread().getName()
                );
                return result;
            } catch (BusinessException exception) {
                if (OpenAiApiExceptionTranslator.isCreditExhausted(exception)) {
                    throw exception;
                }
                lastException = exception;
                if (attempt < maxAttempts) {
                    log.warn(
                            "[DOCUMENT_PERF] content generation AI request retry. examScopeNodeId={}, attempt={}/{}, elapsedMs={}, thread={}",
                            request.examScopeNodeId(),
                            attempt,
                            maxAttempts,
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - attemptStartedAt),
                            Thread.currentThread().getName()
                    );
                    waitBeforeRetry(request.examScopeNodeId(), attempt);
                }
            }
        }

        log.error("목차별 개념 설명 생성 최종 실패. examScopeNodeId={}",
                request.examScopeNodeId(), lastException);
        throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
    }

    @Override
    public List<ConceptExplanationBatchItem> generateBatch(
            List<ConceptExplanationRequest> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }

        String prompt = promptBuilder.buildBatch(requests);
        Set<Long> requestedIds = requests.stream()
                .map(ConceptExplanationRequest::examScopeNodeId)
                .collect(Collectors.toUnmodifiableSet());
        BusinessException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                List<ConceptExplanationBatchItem> results = parseBatch(
                        requestOpenAi(prompt, buildBatchResponseFormat()),
                        requestedIds
                );
                log.info(
                        "목차별 개념 설명 배치 생성 완료. topicCount={}, attempt={}",
                        requests.size(),
                        attempt
                );
                return results;
            } catch (BusinessException exception) {
                if (OpenAiApiExceptionTranslator.isCreditExhausted(exception)) {
                    throw exception;
                }
                lastException = exception;
                if (attempt < maxAttempts) {
                    log.warn(
                            "목차별 개념 설명 배치 생성 재시도. topicCount={}, attempt={}/{}",
                            requests.size(),
                            attempt,
                            maxAttempts
                    );
                    waitBeforeRetry(null, attempt);
                }
            }
        }

        log.error(
                "목차별 개념 설명 배치 생성 최종 실패. topicCount={}",
                requests.size(),
                lastException
        );
        throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
    }

    private void waitBeforeRetry(Long examScopeNodeId, int failedAttempt) {
        long multiplier = 1L << Math.min(Math.max(0, failedAttempt - 1), 4);
        long safeBaseDelay = Math.min(
                retryBackoffMillis,
                MAX_RETRY_BACKOFF_MILLIS / multiplier
        );
        long jitterMillis = retryBackoffMillis == 0
                ? 0
                : ThreadLocalRandom.current().nextLong(retryBackoffMillis + 1);
        long delayMillis = safeBaseDelay * multiplier + jitterMillis;
        if (delayMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn(
                    "목차별 개념 설명 재시도 대기가 중단되었습니다. examScopeNodeId={}",
                    examScopeNodeId,
                    exception
            );
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }

    private String requestOpenAi(
            String prompt,
            Map<String, Object> responseFormat
    ) {
        try {
            OpenAiChatCompletionResponse response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(buildRequestBody(prompt, responseFormat))
                    .retrieve()
                    .body(OpenAiChatCompletionResponse.class);

            String content = response == null ? null : response.getFirstContent();
            if (content == null || content.isBlank()) {
                throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
            }
            return content.trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.error(
                    "OpenAI 개념 설명 생성 HTTP 응답 실패. statusCode={}, responseBody={}",
                    exception.getStatusCode().value(),
                    abbreviate(exception.getResponseBodyAsString(), 500),
                    exception
            );
            throw OpenAiApiExceptionTranslator.translate(
                    exception,
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
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

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength
                ? normalized
                : normalized.substring(0, maxLength) + "...";
    }

    private List<ConceptExplanationBatchItem> parseBatch(
            String response,
            Set<Long> requestedIds
    ) {
        try {
            ConceptExplanationBatchResponse batchResponse = objectMapper.readValue(
                    normalizeResponse(response),
                    ConceptExplanationBatchResponse.class
            );
            if (batchResponse == null || batchResponse.contents() == null
                    || batchResponse.contents().size() != requestedIds.size()) {
                throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
            }

            Set<Long> returnedIds = new HashSet<>();
            List<ConceptExplanationBatchItem> normalized = batchResponse.contents().stream()
                    .map(item -> normalizeBatchItem(item, requestedIds, returnedIds))
                    .toList();
            if (!returnedIds.equals(requestedIds)) {
                throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
            }
            return normalized;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("AI 개념 설명 배치 응답 파싱 실패.", exception);
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }

    private ConceptExplanationBatchItem normalizeBatchItem(
            ConceptExplanationBatchItem item,
            Set<Long> requestedIds,
            Set<Long> returnedIds
    ) {
        if (item == null
                || item.examScopeNodeId() == null
                || !requestedIds.contains(item.examScopeNodeId())
                || !returnedIds.add(item.examScopeNodeId())
                || item.title() == null
                || item.title().isBlank()
                || item.title().trim().length() > 255
                || item.content() == null
                || item.content().isBlank()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
        return new ConceptExplanationBatchItem(
                item.examScopeNodeId(),
                item.title().trim(),
                item.content().trim()
        );
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

    private Map<String, Object> buildRequestBody(
            String prompt,
            Map<String, Object> responseFormat
    ) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
                Map.of("role", "system", "content",
                        "당신은 제공된 학습 자료만 근거로 자격증 개념 설명을 만드는 시스템입니다."),
                Map.of("role", "user", "content", prompt)
        ));
        request.put("temperature", 0.2);
        request.put("max_completion_tokens", maxOutputTokens);
        request.put("response_format", responseFormat);
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

    private Map<String, Object> buildBatchResponseFormat() {
        Map<String, Object> itemProperties = new LinkedHashMap<>();
        itemProperties.put("examScopeNodeId", Map.of("type", "integer"));
        itemProperties.put("title", Map.of("type", "string"));
        itemProperties.put("content", Map.of("type", "string"));

        Map<String, Object> itemSchema = new LinkedHashMap<>();
        itemSchema.put("type", "object");
        itemSchema.put("properties", itemProperties);
        itemSchema.put("required", List.of("examScopeNodeId", "title", "content"));
        itemSchema.put("additionalProperties", false);

        Map<String, Object> rootProperties = Map.of(
                "contents",
                Map.of("type", "array", "items", itemSchema)
        );
        Map<String, Object> rootSchema = new LinkedHashMap<>();
        rootSchema.put("type", "object");
        rootSchema.put("properties", rootProperties);
        rootSchema.put("required", List.of("contents"));
        rootSchema.put("additionalProperties", false);

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "concept_explanation_batch",
                        "strict", true,
                        "schema", rootSchema
                )
        );
    }
}
