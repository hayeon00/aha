package com.aha.domain.ailearn.document.client.mapping;

import com.aha.domain.ailearn.document.dto.mapping.request.ChunkScopeMappingRequestDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentScopeMappingPromptBuilder {

    private final ObjectMapper objectMapper;

    /**
     * 문서 청크와 청크별 목차 후보를 AI 요청 프롬프트로 변환한다.
     */
    public String build(
            List<ChunkScopeMappingRequestDto> requests
    ) {
        validateRequests(requests);

        try {
            String requestsJson =
                    objectMapper.writeValueAsString(requests);

            return """
                    당신은 자격증 학습 문서의 내용을 시험 목차와 연결하는 시스템입니다.

                    전달된 각 문서 청크를 검토하고,
                    해당 청크와 가장 관련성이 높은 시험 목차 후보를 선택하세요.

                    반드시 지켜야 할 규칙:

                    1. 입력에 포함된 documentChunkId만 사용하세요.
                    2. 각 청크에 포함된 scopeCandidates 안의 examScopeNodeId만 사용하세요.
                    3. 관련성이 충분하지 않은 청크는 mappings 결과에서 제외하세요.
                    4. 동일한 documentChunkId와 examScopeNodeId 조합을 중복 반환하지 마세요.
                    5. confidenceScore는 반드시 JSON 숫자로 반환하세요.
                    6. confidenceScore의 범위는 0 이상 1 이하입니다.
                    7. confidenceScore에 문자열, 단위 또는 영문 숫자 단어를 넣지 마세요.
                    8. mappingReason은 해당 목차를 선택한 이유를 짧고 명확하게 작성하세요.
                    9. 반드시 유효한 JSON 객체만 반환하세요.
                    10. 마크다운 코드 블록이나 JSON 외부 설명을 포함하지 마세요.

                    confidenceScore 올바른 예:
                    "confidenceScore": 0.9

                    confidenceScore 잘못된 예:
                    "confidenceScore": "0.9"
                    "confidenceScore": 0. nine
                    "confidenceScore": "높음"
                    "confidenceScore": 90

                    응답 형식:
                    {
                      "mappings": [
                        {
                          "documentChunkId": 1,
                          "examScopeNodeId": 10,
                          "confidenceScore": 0.92,
                          "mappingReason": "청크 내용이 해당 목차의 핵심 개념과 직접적으로 일치함"
                        }
                      ]
                    }

                    문서 청크별 목차 후보:
                    %s
                    """.formatted(requestsJson);

        } catch (JacksonException exception) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
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