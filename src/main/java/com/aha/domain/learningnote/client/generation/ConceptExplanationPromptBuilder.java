package com.aha.domain.learningnote.client.generation;

import com.aha.domain.learningnote.client.generation.dto.ConceptExplanationRequest;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

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
                    4. 적절한 소제목, 목록, 굵은 글씨를 사용하되 불필요하게 장황하게 쓰지 마세요.
                    5. 문서 발췌의 chunkId 같은 내부 식별자는 결과에 노출하지 마세요.
                    6. title은 255자 이내의 간결한 개념 설명 제목이어야 합니다.
                    7. 반드시 제공된 JSON Schema에 맞는 JSON 객체만 반환하세요.

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
