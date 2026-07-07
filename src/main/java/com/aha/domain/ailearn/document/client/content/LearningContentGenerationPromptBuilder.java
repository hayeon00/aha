package com.aha.domain.ailearn.document.client.content;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LearningContentGenerationPromptBuilder
 * @since : 2026. 6. 25. 목요일
 */

@Component
public class LearningContentGenerationPromptBuilder {

    public String build(String topicTitle, String documentContext){

        validateInput(topicTitle, documentContext);

        return """
                당신은 자격증 시험을 준비하는 학습자를 위한
                개념 설명 콘텐츠를 작성하는 전문 강사입니다.

                [현재 학습 목차]
                %s

                현재 목차는 개념 설명이 생성되는 최하위 학습 목차입니다.

                아래 문서 청크에서 현재 목차와 관련된 내용을 찾아,
                학습자가 이해하기 쉬운 하나의 체계적인 개념 설명으로 작성하세요.

                [작성 규칙]
                - 현재 목차의 핵심 개념부터 명확하게 설명하세요.
                - 문서에 포함된 관련 세부 개념을 빠뜨리지 마세요.
                - 여러 문서 청크의 내용을 단순히 나열하지 말고 자연스럽게 통합하세요.
                - 세부 개념이 여러 개라면 Markdown 소제목을 사용할 수 있습니다.
                - 소제목은 현재 목차를 이해하는 데 필요한 경우에만 사용하세요.
                - 세부 개념을 별개의 서비스 목차처럼 표현하지 마세요.
                - 예시가 이해에 도움이 되는 경우 자연스럽게 포함하세요.
                - SQL 설명이 필요한 경우 SQL 코드 블록을 사용할 수 있습니다.
                - 제공된 문서에 없는 내용을 사실처럼 추가하지 마세요.
                - 동일한 내용을 반복해서 설명하지 마세요.
                - "문서에 따르면", "제공된 문서에서는" 같은 표현은 사용하지 마세요.
                - 현재 목차 제목을 본문 첫 줄에서 다시 반복하지 마세요.
                - 별도의 요약, 시험 포인트, 결론 섹션을 강제로 만들지 마세요.
                - 결과는 부가 설명 없이 Markdown 본문만 반환하세요.
                - documentChunkId에는 반드시 [문서 청크 목록]에 제공된 청크 ID만 사용하세요.
                - examScopeNodeId에는 반드시 [목차 후보 목록]에 제공된 목차 ID만 사용하세요.
                - documentChunkId를 examScopeNodeId로 복사하지 마세요.
                - 목차 후보 목록에 없는 ID를 절대로 생성하지 마세요.
                - 적절한 목차가 없으면 해당 청크의 매핑 결과를 반환하지 마세요.
                - ID를 추측하거나 새로 만들지 마세요.

                [문서 청크]
                %s
                """.formatted(
                topicTitle.trim(),
                documentContext.trim()
        );
    }

    private void validateInput(String topicTitle, String documentContext) {

        if(topicTitle == null || topicTitle.trim().isBlank() || documentContext == null || documentContext.trim().isBlank()){

            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

    }
}
