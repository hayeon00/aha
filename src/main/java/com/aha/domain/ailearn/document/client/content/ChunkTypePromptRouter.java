package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import org.springframework.stereotype.Component;

@Component
public class ChunkTypePromptRouter {

    public String instructionFor(DocumentChunkContentType type) {
        return switch (type) {
            case HEADING -> """
                    이 청크는 대주제 또는 소주제 제목입니다.
                    제목을 단순 반복하지 말고 하위에서 학습할 핵심 방향과 구조적 개요를 설명하세요.
                    """;
            case TABLE -> """
                    이 청크는 표 형식의 데이터입니다.
                    행과 열의 관계를 유지하고 비교·대조 항목을 명확히 설명하세요.
                    원본에 표 구조가 있다면 유효한 Markdown 표로 재구성하세요.
                    """;
            case TEXT, MIXED -> """
                    이 청크는 서술형 본문입니다.
                    핵심 개념과 인과관계를 중심으로 가독성 있게 요약하세요.
                    """;
            case CODE, COMMAND, CONFIG -> """
                    이 청크는 코드 또는 명령·설정 예시입니다.
                    원문 코드는 코드 블록으로 보존하고 목적, 주요 구문, 실행 시 주의점을 설명하세요.
                    """;
            case FORMULA -> """
                    이 청크는 공식 또는 계산식입니다.
                    기호의 의미, 적용 조건, 계산 순서를 단계적으로 설명하세요.
                    """;
            case EXAMPLE -> """
                    이 청크는 사례입니다.
                    사례가 보여주는 개념과 판단 근거를 분리해 설명하세요.
                    """;
            case WARNING -> """
                    이 청크는 주의사항입니다.
                    위험 조건과 흔한 실수, 예방 방법을 명확히 강조하세요.
                    """;
        };
    }
}
