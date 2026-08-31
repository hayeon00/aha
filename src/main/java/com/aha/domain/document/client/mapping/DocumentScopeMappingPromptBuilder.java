package com.aha.domain.document.client.mapping;

import com.aha.domain.document.client.mapping.dto.ChunkScopeMappingRequest;
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
            List<ChunkScopeMappingRequest> requests
    ) {
        validateRequests(requests);

        try {
            String requestsJson =
                    objectMapper.writeValueAsString(requests);

            return """
                    당신은 자격증 학습 문서의 내용을 시험 목차와 연결하는 시스템입니다.
            
                    전달된 각 documentChunk를 검토하고,
                    해당 청크에서 실제로 설명하고 있는 시험 목차 Topic을
                    해당 청크의 scopeCandidates 중에서 모두 선택하세요.
            
                    하나의 청크가 여러 시험 Topic을 설명할 수 있으므로,
                    동일한 documentChunkId에 대해 여러 mapping을 반환할 수 있습니다.
            
                    가장 관련성이 높은 Topic 하나만 선택하는 문제가 아닙니다.
                    각 scopeCandidate를 서로 독립적으로 평가하고,
                    청크가 실제로 설명하고 있는 Topic이라면 여러 개를 모두 선택하세요.
            
            
                    반드시 지켜야 할 규칙:
            
                    1. 입력에 포함된 documentChunkId만 사용하세요.
            
                    2. 각 documentChunk의 scopeCandidates에 포함된
                       examScopeNodeId만 사용할 수 있습니다.
            
                    3. 각 documentChunk는 서로 완전히 독립적인 문서 조각으로 판단하세요.
            
                       현재 documentChunk의 mapping을 판단할 때
                       같은 요청에 포함된 다른 documentChunk의 내용을
                       근거로 사용해서는 안 됩니다.
            
                       선택하려는 Topic의 개념, 역할, 특징, 사용법 또는 차이에 대한 근거는
                       반드시 현재 documentChunk의 contentText 안에 직접 존재해야 합니다.
            
                    4. 다른 documentChunk에서 특정 Topic을 자세히 설명하고 있더라도,
                       현재 documentChunk의 contentText에 해당 Topic을 설명하는 근거가 없다면
                       현재 documentChunk의 mapping으로 선택하지 마세요.
            
                    5. mappingReason 역시 반드시 현재 documentChunk의 contentText에
                       실제로 존재하는 내용만 근거로 작성하세요.
            
                       현재 청크에 없는 내용을 만들어내거나,
                       다른 documentChunk의 내용을 섞어서 설명하지 마세요.
            
                    6. 문서 제목, 테스트 자료임을 설명하는 문장, 서비스 설명,
                       문서 작성 목적, 학습 계획, 개발 환경 기록 등
                       시험 개념 자체를 설명하지 않는 메타데이터성 내용은
                       시험 Topic으로 매핑하지 마세요.
            
                    7. 각 scopeCandidate는 서로 독립적으로 평가하세요.
            
                       하나의 Topic을 선택했다는 이유로
                       같은 청크에서 실제로 설명되고 있는 다른 Topic을 제외하지 마세요.
            
                    8. 청크에서 해당 Topic의 다음 내용 중 하나 이상을
                       명시적으로 설명하고 있다면 해당 Topic을 선택할 수 있습니다.
            
                       - 개념 또는 정의
                       - 역할 또는 목적
                       - 동작 원리
                       - 사용 방법
                       - 특징
                       - 다른 개념과의 차이
                       - 해당 Topic에 속하는 대표적인 연산, 문법, 함수 또는 구성 요소
                       - 해당 Topic에 속하는 구체적인 예시
            
                    9. Topic에 대한 설명이 짧더라도
                       해당 Topic의 역할, 의미, 동작 또는 사용 방법이
                       명확하게 설명되어 있다면 선택할 수 있습니다.
            
                    10. documentChunk의 sectionTitle과 시험 Topic의 title이
                        문구상 완전히 동일하지 않더라도,
                        contentText가 해당 Topic의 실제 내용을 설명하고 있다면
                        해당 Topic을 선택하세요.
            
                        제목의 문자열 일치 여부보다
                        현재 청크가 해당 Topic의 개념을 실제로 설명하고 있는지가
                        더 중요한 판단 기준입니다.
            
                    11. 시험 Topic의 대표적인 연산, 문법, 함수 또는 구성 요소를
                        현재 청크에서 직접 설명하고 있다면
                        sectionTitle의 표현이 시험 Topic title과 조금 다르더라도
                        해당 Topic으로 판단할 수 있습니다.
            
                        예를 들어 시험 Topic이 "집합 연산자"이고
                        현재 documentChunk에서 UNION, UNION ALL 등의 동작과 차이,
                        사용 조건을 설명하고 있다면
                        sectionTitle이 "집합 연산"이더라도
                        "집합 연산자" Topic을 선택해야 합니다.
            
                        단순히 제목 표현이 비슷하기 때문에 선택하는 것이 아니라,
                        contentText에서 해당 Topic의 실제 내용을 설명하고 있을 때만
                        선택해야 합니다.
            
                    12. 다른 Topic을 설명하는 과정에서 함께 등장한 Topic도
                        그 Topic 자체의 역할이나 차이가 독립적으로 설명되어 있다면
                        별도의 mapping으로 선택하세요.
            
                        예를 들어 현재 청크에서
            
                        WHERE는 그룹화 이전의 행을 필터링하고,
                        HAVING은 GROUP BY 이후의 그룹을 필터링한다고 설명한다면,
            
                        WHERE 관련 Topic과 GROUP BY/HAVING 관련 Topic을
                        각각 독립적으로 평가할 수 있습니다.
            
                    13. 단순히 Topic 이름, 관련 단어 또는 키워드가
                        contentText에 등장했다는 이유만으로 선택하지 마세요.
            
                        해당 Topic의 의미나 역할을 실제로 설명하는 내용이 있어야 합니다.
            
                    14. 청크와 의미적으로 가까운 분야라는 이유만으로
                        Topic을 선택하지 마세요.
            
                        해당 Topic 자체의 개념, 역할, 동작 또는 사용법을 설명하는
                        명확한 근거가 현재 documentChunk 안에 있어야 합니다.
            
                    15. 상위 개념이나 인접 개념이라는 이유만으로
                        자동으로 Topic을 선택하지 마세요.
            
                        예를 들어 JOIN을 설명한다고 해서
                        관계형 데이터베이스, 데이터 모델의 관계,
                        관계와 조인의 이해 등 인접 Topic을 자동으로 선택해서는 안 됩니다.
            
                        해당 Topic 자체에 대한 설명이
                        현재 청크에 존재하는 경우에만 선택하세요.
            
                    16. 관련성이 충분하지 않은 후보는 mappings 결과에서 제외하세요.
            
                        모든 scopeCandidate 중 반드시 하나 이상을 선택할 필요는 없습니다.
            
                    17. 동일한 documentChunkId와 examScopeNodeId 조합을
                        중복 반환하지 마세요.
            
                    18. confidenceScore는
            
                        "현재 documentChunk가 해당 Topic을 실제로 설명하고 있다고
                        판단하는 확신도"
            
                        를 의미하며, 반드시 0 이상 1 이하의 JSON 숫자로 반환하세요.
            
                    19. confidenceScore는 입력으로 제공된 embedding similarity 값을
                        그대로 복사하거나 변환한 값이 아닙니다.
            
                        현재 documentChunk의 contentText와 Topic 사이의
                        실제 설명 관계를 기준으로 독립적으로 판단하세요.
            
                    20. confidenceScore에 문자열, 단위 또는
                        영문 숫자 단어를 넣지 마세요.
            
                        올바른 예:
                        "confidenceScore": 0.9
            
                        잘못된 예:
                        "confidenceScore": "0.9"
                        "confidenceScore": 0. nine
                        "confidenceScore": "높음"
                        "confidenceScore": 90
            
                    21. mappingReason에는 단순히
                        "관련성이 높음", "연관성이 있음"이라고 작성하지 마세요.
            
                        현재 documentChunk의 어떤 내용 때문에
                        해당 Topic을 선택했는지 짧고 구체적으로 작성하세요.
            
                    22. 반드시 유효한 JSON 객체만 반환하세요.
            
                    23. 마크다운 코드 블록이나 JSON 외부 설명을 포함하지 마세요.
            
            
                    응답 형식:
            
                    {
                      "mappings": [
                        {
                          "documentChunkId": 1,
                          "examScopeNodeId": 10,
                          "confidenceScore": 0.92,
                          "mappingReason": "청크에서 해당 Topic의 개념과 역할을 직접 설명함"
                        },
                        {
                          "documentChunkId": 1,
                          "examScopeNodeId": 11,
                          "confidenceScore": 0.84,
                          "mappingReason": "청크에서 해당 Topic의 사용법과 다른 개념과의 차이를 설명함"
                        }
                      ]
                    }
            
            
                    하나의 청크에서 여러 Topic을 실제로 설명한다면
                    동일한 documentChunkId에 대해 여러 mapping을 반환하세요.
            
                    예:
            
                    현재 documentChunk의 contentText:
            
                    "SELECT 문은 데이터를 조회한다.
                    WHERE 절은 그룹화 이전에 행을 필터링한다.
                    GROUP BY는 행을 그룹화하고,
                    HAVING은 그룹화 이후의 결과를 필터링한다.
                    ORDER BY는 결과의 정렬 순서를 결정한다.
                    COUNT, SUM, AVG는 집계 함수로 사용한다."
            
                    그리고 scopeCandidates에 각각 대응하는 Topic이 존재한다면
            
                    - SELECT 관련 Topic
                    - WHERE 관련 Topic
                    - GROUP BY/HAVING 관련 Topic
                    - ORDER BY 관련 Topic
                    - 집계 함수 관련 Topic
            
                    을 각각 독립적으로 선택할 수 있습니다.
            
            
                    제목 표현이 다르더라도 실제 개념이 일치하는 경우의 예:
            
                    현재 documentChunk:
            
                    sectionTitle:
                    "집합 연산"
            
                    contentText:
                    "UNION은 두 SELECT 결과를 합치면서 중복을 제거한다.
                    UNION ALL은 중복을 제거하지 않고 모든 행을 유지한다."
            
                    scopeCandidates에 "집합 연산자" Topic이 있다면,
                    현재 청크는 집합 연산자의 대표 문법인 UNION과 UNION ALL의
                    동작과 차이를 직접 설명하고 있으므로
                    해당 Topic을 선택할 수 있습니다.
            
            
                    반대로 현재 documentChunk가 시험 개념을 설명하지 않거나
                    scopeCandidates 중 실제로 설명하고 있는 Topic이 하나도 없다면
                    다음처럼 빈 배열을 반환하세요.
            
                    {
                      "mappings": []
                    }
            
            
                    특히 주의하세요:
            
                    여러 documentChunk가 하나의 요청에 함께 포함되어 있더라도
                    청크 간 내용을 합쳐서 판단해서는 안 됩니다.
            
                    각 mapping의 근거는 반드시
                    해당 documentChunkId의 contentText 안에서만 찾아야 합니다.
            
            
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
            List<ChunkScopeMappingRequest> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
            );
        }
    }
}