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

    public String build(List<ChunkScopeMappingRequestDto> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        try {
            String requestsJson = objectMapper.writeValueAsString(requests);

            return """
                    당신은 자격증 학습 문서를 시험 목차에 매핑하는 시스템입니다.

                    각 문서 청크를 가장 관련 있는 시험 목차에 매핑하세요.

                    매우 중요한 규칙:
                    1. 각 chunk는 반드시 같은 객체 안에 있는 scopeCandidates 목록 중에서만 매핑하세요.
                    2. 다른 chunk의 scopeCandidates를 참조하면 안 됩니다.
                    3. 후보 목록에 없는 examScopeNodeId를 반환하면 안 됩니다.
                    4. 관련성이 낮은 청크는 결과에서 제외하세요.
                    5. confidenceScore는 0 이상 1 이하로 반환하세요.
                    6. 동일한 documentChunkId와 examScopeNodeId 조합을 중복 반환하지 마세요.
                    7. 반드시 JSON 형식으로만 응답하세요.
                    8. 설명 문장이나 마크다운 코드 블록을 포함하지 마세요.

                    응답 형식:
                    {
                      "mappings": [
                        {
                          "documentChunkId": 1,
                          "examScopeNodeId": 10,
                          "confidenceScore": 0.92,
                          "mappingReason": "매핑 이유"
                        }
                      ]
                    }

                    입력 데이터:
                    각 항목은 chunk 1개와 해당 chunk에 대한 Top 후보 목차 목록입니다.

                    %s
                    """.formatted(requestsJson);

        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }
    }
}