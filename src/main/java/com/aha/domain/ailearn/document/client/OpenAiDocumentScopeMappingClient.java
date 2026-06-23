package com.aha.domain.ailearn.document.client;

import com.aha.domain.ailearn.document.dto.mapping.request.ExamScopeCandidateDto;
import com.aha.domain.ailearn.document.dto.mapping.response.DocumentScopeMappingResultDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class OpenAiDocumentScopeMappingClient
        implements DocumentScopeMappingClient {

    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    @Override
    public DocumentScopeMappingResultDto mapChunk(
            String chunkContent,
            List<ExamScopeCandidateDto> candidates
    ) {
        validateRequest(chunkContent, candidates);

        String prompt = buildPrompt(
                chunkContent,
                candidates
        );

        try {
            String responseBody = webClient.post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            "Authorization",
                            "Bearer " + apiKey
                    )
                    .bodyValue(
                            buildRequestBody(prompt)
                    )
                    .retrieve()
                    .onStatus(
                            status ->
                                    status.is4xxClientError()
                                            || status.is5xxServerError(),
                            response ->
                                    response.bodyToMono(String.class)
                                            .map(errorBody -> {
                                                log.error(
                                                        "OpenAI 목차 매핑 요청 실패. status={}, body={}",
                                                        response.statusCode(),
                                                        errorBody
                                                );

                                                return new BusinessException(
                                                        ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
                                                );
                                            })
                    )
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null
                    || responseBody.isBlank()) {

                throw new BusinessException(
                        ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
                );
            }

            JsonNode response =
                    objectMapper.readTree(responseBody);

            String outputText =
                    extractOutputText(response);

            if (outputText == null
                    || outputText.isBlank()) {

                log.error(
                        "OpenAI 목차 매핑 응답 본문이 비어 있습니다. responseBody={}",
                        responseBody
                );

                throw new BusinessException(
                        ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
                );
            }

            DocumentScopeMappingResultDto result =
                    parseResult(outputText);

            validateResult(
                    result,
                    candidates
            );

            return result;

        } catch (BusinessException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "AI 문서 목차 매핑 실패",
                    e
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
    }

    private Map<String, Object> buildRequestBody(
            String prompt
    ) {
        return Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", buildSystemPrompt()
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.1,
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "document_scope_mapping_result",
                                "strict", true,
                                "schema", buildJsonSchema()
                        )
                )
        );
    }

    private String buildSystemPrompt() {
        return """
                너는 자격증 학습 문서를 시험 목차에 분류하는 AI다.

                역할:
                - 제공된 문서 청크의 핵심 주제를 분석한다.
                - 제공된 목차 후보 중 가장 적절한 목차 하나를 선택한다.
                - 후보 목록에 존재하지 않는 ID를 생성하지 않는다.

                판단 기준:
                - 단순히 같은 단어가 있는지만 보지 말고 청크의 핵심 개념을 판단한다.
                - 상위 개념보다 가장 구체적으로 일치하는 최하위 목차를 선택한다.
                - 청크에서 가장 많은 비중을 차지하는 주제를 기준으로 선택한다.
                - mappingReason에는 선택 이유를 짧고 명확하게 작성한다.

                출력 규칙:
                - 반드시 JSON 형식만 출력한다.
                - 마크다운 코드 블록은 사용하지 않는다.
                """;
    }

    private String buildPrompt(
            String chunkContent,
            List<ExamScopeCandidateDto> candidates
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("""
                다음 문서 청크를 가장 적절한 시험 목차 하나에 매핑하세요.

                [문서 청크]
                """);

        builder.append(chunkContent);

        builder.append("\n\n[시험 목차 후보]\n");

        for (ExamScopeCandidateDto candidate : candidates) {
            builder.append("- examScopeNodeId: ")
                    .append(candidate.examScopeNodeId())
                    .append("\n  title: ")
                    .append(candidate.title())
                    .append("\n  path: ")
                    .append(candidate.path())
                    .append("\n");
        }

        return builder.toString();
    }

    private Map<String, Object> buildJsonSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of(
                        "examScopeNodeId",
                        "mappingReason"
                ),
                "properties", Map.of(
                        "examScopeNodeId", Map.of(
                                "type", "integer"
                        ),
                        "mappingReason", Map.of(
                                "type", "string",
                                "minLength", 1,
                                "maxLength", 1000
                        )
                )
        );
    }

    private String extractOutputText(
            JsonNode response
    ) {
        if (response == null) {
            return null;
        }

        JsonNode output = response.get("output");

        if (output == null || !output.isArray()) {
            return null;
        }

        for (JsonNode outputItem : output) {
            JsonNode content =
                    outputItem.get("content");

            if (content == null || !content.isArray()) {
                continue;
            }

            for (JsonNode contentItem : content) {
                if ("output_text".equals(
                        contentItem.path("type").asText()
                )) {
                    return contentItem
                            .path("text")
                            .asText();
                }
            }
        }

        return null;
    }

    private DocumentScopeMappingResultDto parseResult(
            String outputText
    ) {
        try {
            return objectMapper.readValue(
                    outputText,
                    DocumentScopeMappingResultDto.class
            );

        } catch (Exception e) {
            log.error(
                    "AI 목차 매핑 응답 파싱 실패. outputText={}",
                    outputText,
                    e
            );

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }

    private void validateRequest(
            String chunkContent,
            List<ExamScopeCandidateDto> candidates
    ) {
        if (chunkContent == null
                || chunkContent.isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        if (candidates == null
                || candidates.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.EXAM_SCOPE_NODE_NOT_FOUND
            );
        }
    }
    private void validateResult(
            DocumentScopeMappingResultDto result,
            List<ExamScopeCandidateDto> candidates
    ) {
        if (result == null
                || result.examScopeNodeId() == null
                || result.mappingReason() == null
                || result.mappingReason().isBlank()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }

        boolean validScopeNodeId =
                candidates.stream()
                        .anyMatch(candidate ->
                                candidate.examScopeNodeId()
                                        .equals(
                                                result.examScopeNodeId()
                                        )
                        );

        if (!validScopeNodeId) {
            log.error(
                    "AI가 후보에 없는 목차 ID를 반환했습니다. examScopeNodeId={}",
                    result.examScopeNodeId()
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
    }
}