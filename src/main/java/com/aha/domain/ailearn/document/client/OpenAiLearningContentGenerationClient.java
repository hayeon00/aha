package com.aha.domain.ailearn.document.client;

import com.aha.domain.ailearn.document.dto.generation.request.TopicLearningContentSourceDto;
import com.aha.domain.ailearn.document.dto.generation.response.LearningContentGenerationResultDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class OpenAiLearningContentGenerationClient implements LearningContentGenerationClient {

    private static final int MAX_KEYWORD_COUNT = 10;

    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .build();

    /**
     * 특정 시험 목차에 매핑된 문서 청크를 바탕으로
     * 사용자에게 보여줄 개념 설명을 생성한다.
     */
    @Override
    public LearningContentGenerationResultDto generate(
            TopicLearningContentSourceDto source
    ) {
        validateRequest(source);

        String prompt =
                buildPrompt(source);

        log.info(
                "OpenAI 개념 설명 생성 요청 시작. examScopeNodeId={}, scopeTitle={}, chunkCount={}",
                source.examScopeNodeId(),
                source.scopeTitle(),
                source.chunkContents().size()
        );

        try {
            String responseBody =
                    webClient.post()
                            .uri("/responses")
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
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
                                                                "OpenAI 개념 설명 생성 요청 실패. status={}, body={}",
                                                                response.statusCode(),
                                                                errorBody
                                                        );

                                                        return new BusinessException(
                                                                ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
                                                        );
                                                    })
                            )
                            .bodyToMono(String.class)
                            .block();

            if (responseBody == null
                    || responseBody.isBlank()) {

                log.error(
                        "OpenAI 개념 설명 응답이 비어 있습니다. examScopeNodeId={}",
                        source.examScopeNodeId()
                );

                throw new BusinessException(
                        ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
                );
            }

            JsonNode response =
                    objectMapper.readTree(responseBody);

            String outputText =
                    extractOutputText(response);

            if (outputText == null
                    || outputText.isBlank()) {

                log.error(
                        "OpenAI 개념 설명 output_text가 비어 있습니다. examScopeNodeId={}, responseBody={}",
                        source.examScopeNodeId(),
                        responseBody
                );

                throw new BusinessException(
                        ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
                );
            }


            LearningContentGenerationResultDto result =
                    parseResult(outputText);

            validateResult(result);

            log.info(
                    "OpenAI 개념 설명 생성 응답 완료. examScopeNodeId={}, title={}, keywordCount={}",
                    source.examScopeNodeId(),
                    result.title(),
                    result.keywords() == null
                            ? 0
                            : result.keywords().size()
            );

            return result;

        } catch (BusinessException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "AI 개념 설명 생성 중 예외가 발생했습니다. examScopeNodeId={}",
                    source.examScopeNodeId(),
                    exception
            );

            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }
    }

    /**
     * OpenAI Responses API 요청 본문을 생성한다.
     */
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
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "learning_content_generation_result",
                                "strict", true,
                                "schema", buildJsonSchema()
                        )
                )
        );
    }

    /**
     * AI 역할과 개념 설명 작성 원칙을 정의한다.
     */
    private String buildSystemPrompt() {
        return """
                너는 자격증 학습 문서를 학습자가 이해하기 쉬운 개념 설명으로
                재구성하는 전문 교육 콘텐츠 작성 AI다.

                제공되는 정보:
                - 시험 목차 제목
                - 시험 목차 전체 경로
                - 해당 목차에 매핑된 문서 청크 목록

                작성 원칙:
                1. 제공된 문서 청크를 주요 근거로 사용한다.
                2. 청크를 단순히 이어 붙이지 않고 중복 내용을 제거해 재구성한다.
                3. 현재 목차와 직접 관련 없는 내용은 제외한다.
                4. 학습자가 처음 읽어도 이해할 수 있도록 자연스럽게 설명한다.
                5. 중요한 용어는 처음 등장할 때 의미를 함께 설명한다.
                6. 필요한 경우 소제목, 목록, 표, 예시를 사용한다.
                7. SQL 예시가 필요한 경우 Markdown 코드 블록으로 작성한다.
                8. 문서에 없는 내용을 사실처럼 과도하게 확장하지 않는다.
                9. 출처 문서나 청크 번호를 본문에 직접 노출하지 않는다.
                10. 단순 요약이 아니라 학습에 사용할 수 있는 완성된 설명을 작성한다.

                content 작성 형식:
                - Markdown 형식으로 작성한다.
                - 최상위 제목은 별도로 넣지 않는다.
                - 필요한 경우 ##, ### 수준의 소제목을 사용한다.
                - 문단 사이에는 빈 줄을 둔다.
                - 핵심 내용은 목록으로 정리할 수 있다.
                - 읽기 어렵게 지나치게 긴 한 문단으로 작성하지 않는다.

                keywords 작성 기준:
                - 본문에서 실제로 중요하게 다룬 핵심 용어만 선택한다.
                - 최대 10개까지만 반환한다.
                - 중복된 키워드는 포함하지 않는다.

                출력 규칙:
                - 반드시 지정된 JSON 형식만 반환한다.
                - JSON 바깥의 설명은 작성하지 않는다.
                - Markdown 코드 블록으로 JSON 전체를 감싸지 않는다.
                """;
    }

    /**
     * 목차 정보와 관련 청크를 사용자 프롬프트로 구성한다.
     */
    private String buildPrompt(
            TopicLearningContentSourceDto source
    ) {
        StringBuilder builder =
                new StringBuilder();

        builder.append("[시험 목차]\n")
                .append("examScopeNodeId: ")
                .append(source.examScopeNodeId())
                .append("\n")
                .append("제목: ")
                .append(source.scopeTitle())
                .append("\n")
                .append("전체 경로: ")
                .append(source.scopePath())
                .append("\n\n");

        builder.append("[관련 문서 청크]\n");

        for (int index = 0;
             index < source.chunkContents().size();
             index++) {

            String chunkContent =
                    source.chunkContents().get(index);

            builder.append("\n--- 청크 ")
                    .append(index + 1)
                    .append(" ---\n")
                    .append(chunkContent)
                    .append("\n");
        }

        builder.append("""
                
                위 청크들을 종합하여 현재 목차에 해당하는
                완성된 개념 설명을 생성하세요.

                title은 목차 내용을 정확히 나타내는 간결한 제목으로 작성하고,
                content는 사용자가 바로 학습할 수 있는 Markdown 본문으로 작성하세요.
                """);

        return builder.toString();
    }

    /**
     * OpenAI의 구조화 응답 JSON Schema를 생성한다.
     */
    private Map<String, Object> buildJsonSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of(
                        "title",
                        "content",
                        "keywords"
                ),
                "properties", Map.of(
                        "title", Map.of(
                                "type", "string",
                                "minLength", 1,
                                "maxLength", 200
                        ),
                        "content", Map.of(
                                "type", "string",
                                "minLength", 1
                        ),
                        "keywords", Map.of(
                                "type", "array",
                                "maxItems", MAX_KEYWORD_COUNT,
                                "items", Map.of(
                                        "type", "string",
                                        "minLength", 1,
                                        "maxLength", 100
                                )
                        )
                )
        );
    }

    /**
     * Responses API 응답에서 output_text 값을 추출한다.
     */
    private String extractOutputText(
            JsonNode response
    ) {
        if (response == null) {
            return null;
        }

        JsonNode output =
                response.get("output");

        if (output == null
                || !output.isArray()) {
            return null;
        }

        for (JsonNode outputItem : output) {
            JsonNode content =
                    outputItem.get("content");

            if (content == null
                    || !content.isArray()) {
                continue;
            }

            for (JsonNode contentItem : content) {
                String contentType =
                        contentItem.path("type").asText();

                if ("output_text".equals(contentType)) {
                    return contentItem
                            .path("text")
                            .asText();
                }
            }
        }

        return null;
    }

    /**
     * AI의 JSON 응답을 DTO로 변환한다.
     */
    private LearningContentGenerationResultDto parseResult(
            String outputText
    ) {
        try {
            return objectMapper.readValue(
                    outputText,
                    LearningContentGenerationResultDto.class
            );

        } catch (Exception exception) {
            log.error(
                    "AI 개념 설명 응답 파싱 실패. outputText={}",
                    outputText,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }


    private void validateRequest(TopicLearningContentSourceDto source) {
        if (source == null
                || source.examScopeNodeId() == null
                || source.scopeTitle() == null
                || source.scopeTitle().isBlank()
                || source.scopePath() == null
                || source.scopePath().isBlank()
                || source.chunkContents() == null
                || source.chunkContents().isEmpty()) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        boolean hasValidChunk =
                source.chunkContents()
                        .stream()
                        .anyMatch(chunk ->
                                chunk != null
                                        && !chunk.isBlank()
                        );

        if (!hasValidChunk) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }
    }

    /**
     * AI가 반환한 개념 설명 결과를 검증한다.
     */
    private void validateResult(
            LearningContentGenerationResultDto result
    ) {
        if (result == null
                || result.title() == null
                || result.title().isBlank()
                || result.content() == null
                || result.content().isBlank()
                || result.keywords() == null) {

            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }

        if (result.title().length() > 200) {
            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }

        if (result.keywords().size()
                > MAX_KEYWORD_COUNT) {

            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }

        boolean hasInvalidKeyword =
                result.keywords()
                        .stream()
                        .anyMatch(keyword ->
                                keyword == null
                                        || keyword.isBlank()
                                        || keyword.length() > 100
                        );

        if (hasInvalidKeyword) {
            throw new BusinessException(
                    ErrorCode.LEARNING_CONTENT_GENERATION_FAILED
            );
        }
    }
}