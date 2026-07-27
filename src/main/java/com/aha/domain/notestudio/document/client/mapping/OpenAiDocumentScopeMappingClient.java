package com.aha.domain.notestudio.document.client.mapping;

import com.aha.domain.notestudio.document.client.mapping.dto.OpenAiChatCompletionResponse;
import com.aha.domain.notestudio.document.dto.mapping.request.ChunkScopeMappingRequestDto;
import com.aha.domain.notestudio.document.dto.mapping.response.ScopeMappingAiResultResponseDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiDocumentScopeMappingClient
        implements DocumentScopeMappingClient {

    private static final int MAX_ATTEMPTS = 2;

    private final RestClient openAiRestClient;
    private final DocumentScopeMappingPromptBuilder promptBuilder;
    private final DocumentScopeMappingResponseParser responseParser;
    private final String model;

    public OpenAiDocumentScopeMappingClient(
            @Qualifier("openAiRestClient")
            RestClient openAiRestClient,
            DocumentScopeMappingPromptBuilder promptBuilder,
            DocumentScopeMappingResponseParser responseParser,
            @Value("${openAI.model:gpt-4.1-mini}")
            String model
    ) {
        this.openAiRestClient = openAiRestClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
        this.model = model;
    }

    @Override
    public List<ScopeMappingAiResultResponseDto> mapChunks(
            List<ChunkScopeMappingRequestDto> requests
    ) {
        validateRequests(requests);

        String prompt = promptBuilder.build(requests);

        int totalCandidateCount =
                calculateTotalCandidateCount(requests);

        log.info(
                "OpenAI 목차 매핑 요청 시작. requestCount={}, totalCandidateCount={}",
                requests.size(),
                totalCandidateCount
        );

        BusinessException lastException = null;

        for (int attempt = 1;
             attempt <= MAX_ATTEMPTS;
             attempt++) {

            try {
                String aiResponse = requestOpenAi(prompt);

                List<ScopeMappingAiResultResponseDto> results =
                        responseParser.parse(aiResponse);

                log.info(
                        "OpenAI 목차 매핑 요청 완료. attempt={}, mappingCount={}",
                        attempt,
                        results.size()
                );

                return results;

            } catch (BusinessException exception) {
                lastException = exception;

                if (attempt >= MAX_ATTEMPTS) {
                    break;
                }

                log.warn(
                        "OpenAI 목차 매핑 응답 처리 실패로 재요청합니다. attempt={}/{}, requestCount={}",
                        attempt,
                        MAX_ATTEMPTS,
                        requests.size()
                );
            }
        }

        log.error(
                "OpenAI 목차 매핑 요청이 최종 실패했습니다. requestCount={}, maxAttempts={}",
                requests.size(),
                MAX_ATTEMPTS,
                lastException
        );

        throw new BusinessException(
                ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
        );
    }

    /**
     * OpenAI Chat Completions API를 호출한다.
     */
    private String requestOpenAi(
            String prompt
    ) {
        try {
            Map<String, Object> requestBody =
                    buildRequestBody(prompt);

            OpenAiChatCompletionResponse response =
                    openAiRestClient.post()
                            .uri("/chat/completions")
                            .body(requestBody)
                            .retrieve()
                            .body(
                                    OpenAiChatCompletionResponse.class
                            );

            if (response == null) {
                throw new BusinessException(
                        ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
                );
            }

            String content = response.getFirstContent();

            if (content == null || content.isBlank()) {
                throw new BusinessException(
                        ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
                );
            }

            return content.trim();

        } catch (BusinessException exception) {
            throw exception;

        } catch (RestClientException exception) {
            log.error(
                    "OpenAI 목차 매핑 HTTP 요청 실패.",
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );

        } catch (RuntimeException exception) {
            log.error(
                    "OpenAI 목차 매핑 요청 처리 실패.",
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
    }

    /**
     * OpenAI 요청 본문을 만든다.
     *
     * response_format의 JSON Schema를 통해
     * confidenceScore를 0~1 범위의 number로 강제한다.
     */
    private Map<String, Object> buildRequestBody(
            String prompt
    ) {
        Map<String, Object> requestBody =
                new LinkedHashMap<>();

        requestBody.put("model", model);
        requestBody.put(
                "messages",
                List.of(
                        Map.of(
                                "role",
                                "system",
                                "content",
                                """
                                당신은 자격증 학습 문서와 시험 목차를 연결하는 시스템입니다.
                                반드시 제공된 JSON Schema를 준수하여 응답하세요.
                                """
                        ),
                        Map.of(
                                "role",
                                "user",
                                "content",
                                prompt
                        )
                )
        );

        requestBody.put("temperature", 0.0);
        requestBody.put(
                "response_format",
                buildResponseFormat()
        );

        return requestBody;
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> mappingProperties =
                new LinkedHashMap<>();

        mappingProperties.put(
                "documentChunkId",
                Map.of(
                        "type",
                        "integer"
                )
        );

        mappingProperties.put(
                "examScopeNodeId",
                Map.of(
                        "type",
                        "integer"
                )
        );

        mappingProperties.put(
                "confidenceScore",
                Map.of(
                        "type",
                        "number",
                        "minimum",
                        0,
                        "maximum",
                        1
                )
        );

        mappingProperties.put(
                "mappingReason",
                Map.of(
                        "type",
                        "string"
                )
        );

        Map<String, Object> mappingItem =
                new LinkedHashMap<>();

        mappingItem.put("type", "object");
        mappingItem.put(
                "properties",
                mappingProperties
        );
        mappingItem.put(
                "required",
                List.of(
                        "documentChunkId",
                        "examScopeNodeId",
                        "confidenceScore",
                        "mappingReason"
                )
        );
        mappingItem.put(
                "additionalProperties",
                false
        );

        Map<String, Object> mappingsArray =
                new LinkedHashMap<>();

        mappingsArray.put("type", "array");
        mappingsArray.put("items", mappingItem);

        Map<String, Object> rootProperties =
                new LinkedHashMap<>();

        rootProperties.put(
                "mappings",
                mappingsArray
        );

        Map<String, Object> rootSchema =
                new LinkedHashMap<>();

        rootSchema.put("type", "object");
        rootSchema.put(
                "properties",
                rootProperties
        );
        rootSchema.put(
                "required",
                List.of("mappings")
        );
        rootSchema.put(
                "additionalProperties",
                false
        );

        Map<String, Object> jsonSchema =
                new LinkedHashMap<>();

        jsonSchema.put(
                "name",
                "document_scope_mapping"
        );
        jsonSchema.put("strict", true);
        jsonSchema.put("schema", rootSchema);

        Map<String, Object> responseFormat =
                new LinkedHashMap<>();

        responseFormat.put(
                "type",
                "json_schema"
        );
        responseFormat.put(
                "json_schema",
                jsonSchema
        );

        return responseFormat;
    }

    private int calculateTotalCandidateCount(
            List<ChunkScopeMappingRequestDto> requests
    ) {
        return requests.stream()
                .mapToInt(request ->
                        request.scopeCandidates() == null
                                ? 0
                                : request.scopeCandidates().size()
                )
                .sum();
    }

    private void validateRequests(
            List<ChunkScopeMappingRequestDto> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
    }
}