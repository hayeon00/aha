package com.aha.domain.learningnote.client.generation;

import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConceptExplanationPromptBuilder {

    private final ObjectMapper objectMapper;

    public String build(ConceptExplanationRequest request) {
        validate(request);

        try {
            String sourceJson = objectMapper.writeValueAsString(request.sourceChunks());

            return """
                    당신은 자격증 시험 대비 학습노트를 작성하는 전문 튜터입니다.

                    아래 문서 발췌만 근거로 시험 목차 '%s'의 개념 설명을 작성하세요.

                    작성 규칙:
                    1. 원문에 없는 사실을 추측하거나 추가하지 마세요.
                    2. 시험 대비에 필요한 정의, 핵심 원리, 구분 기준과 주의점을 우선하세요.
                    3. content는 읽기 쉬운 한국어 Markdown으로 작성하세요.
                    4. content는 핵심 내용만 작성하고 전체 600자를 넘기지 마세요.
                    5. 소제목은 최대 3개만 사용하고 같은 의미를 반복하지 마세요.
                    6. 문서 발췌의 chunkId 같은 내부 식별자는 결과에 노출하지 마세요.
                    7. title은 255자 이내의 간결한 개념 설명 제목이어야 합니다.
                    8. 반드시 제공된 JSON Schema에 맞는 JSON 객체만 반환하세요.

                    시험 목차 ID: %d
                    시험 목차명: %s
                    문서 발췌:
                    %s
                    """.formatted(
                    request.topicTitle(),
                    request.examScopeNodeId(),
                    request.topicTitle(),
                    sourceJson
            );
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }

    public String buildBatch(List<ConceptExplanationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
        requests.forEach(this::validate);

        try {
            String requestsJson = objectMapper.writeValueAsString(requests);
            return """
                    당신은 자격증 시험 대비 학습노트를 작성하는 전문 튜터입니다.

                    아래 topics 각각에 대해 제공된 문서 발췌만 근거로 개념 설명을 작성하세요.

                    작성 규칙:
                    1. 요청에 포함된 모든 examScopeNodeId에 대해 정확히 하나의 결과를 반환하세요.
                    2. 요청에 없는 examScopeNodeId를 만들지 마세요.
                    3. 각 토픽의 sourceChunks만 근거로 사용하고 다른 토픽 자료를 섞지 마세요.
                    4. 원문에 없는 사실을 추측하거나 추가하지 마세요.
                    5. content는 읽기 쉬운 한국어 Markdown으로 작성하세요.
                    6. 각 content는 핵심 내용만 작성하고 600자를 넘기지 마세요.
                    7. 같은 의미를 반복하지 마세요.
                    8. title은 255자 이내로 작성하세요.
                    9. 반드시 제공된 JSON Schema에 맞는 JSON 객체만 반환하세요.

                    topics:
                    %s
                    """.formatted(requestsJson);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }

    private void validate(ConceptExplanationRequest request) {
        if (request == null
                || request.examScopeNodeId() == null
                || request.topicTitle() == null
                || request.topicTitle().isBlank()
                || request.sourceChunks() == null
                || request.sourceChunks().isEmpty()) {
            throw new BusinessException(ErrorCode.LEARNING_CONTENT_GENERATION_FAILED);
        }
    }
}
