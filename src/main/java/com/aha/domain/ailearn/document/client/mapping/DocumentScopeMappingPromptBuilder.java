package com.aha.domain.ailearn.document.client.mapping;

import com.aha.domain.ailearn.document.dto.mapping.request.ChunkMappingRequestDto;
import com.aha.domain.ailearn.document.dto.mapping.request.ScopeCandidateRequestDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentScopeMappingPromptBuilder
 * @since : 2026. 6. 24. 수요일
 */

@Component
@RequiredArgsConstructor
public class DocumentScopeMappingPromptBuilder {

    private final ObjectMapper objectMapper;

    public String build(List<ChunkMappingRequestDto> chunks, List<ScopeCandidateRequestDto> scopeCandidates){

        if(chunks==null || chunks.isEmpty()){
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        if(scopeCandidates==null || scopeCandidates.isEmpty()){
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

        try{

            String chunksJson = objectMapper.writeValueAsString(chunks);

            String scopeNodesJson = objectMapper.writeValueAsString(scopeCandidates);

            return """
                    당신은 자격증 학습 문서를 시험 목차에 매핑하는 시스템입니다.

                    각 문서 청크를 가장 관련 있는 시험 목차에 매핑하세요.

                    규칙:
                    1. 전달받은 documentChunkId와 examScopeNodeId만 사용하세요.
                    2. 관련성이 낮은 청크는 결과에서 제외하세요.
                    3. confidenceScore는 0 이상 1 이하로 반환하세요.
                    4. 동일한 청크와 목차 조합을 중복 반환하지 마세요.
                    5. 반드시 JSON 형식으로만 응답하세요.
                    6. 설명 문장이나 마크다운 코드 블록을 포함하지 마세요.

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

                    문서 청크:
                    %s

                    시험 목차:
                    %s
                    """.formatted(
                    chunksJson,
                    scopeNodesJson
            );

        }catch (JacksonException exception){
            throw new BusinessException(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
        }

    }

}
