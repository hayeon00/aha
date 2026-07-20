package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.service.content.model.TopicDocumentContext;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.aha.domain.ailearn.document.service.content.model.TopicDocumentChunk;

@Component
@RequiredArgsConstructor
public class LearningContentPromptBuilder {

    private final ChunkTypePromptRouter chunkTypePromptRouter;

    public String buildSystemPrompt() {
        return """
                당신은 자격증 시험 학습을 돕는 전문 교육 콘텐츠 작성자입니다.

                제공된 문서 내용만을 근거로 개념 설명을 작성하세요.

                작성 원칙:
                1. 문서에 없는 내용을 임의로 추가하지 마세요.
                2. 학습자가 이해하기 쉬운 표현을 사용하세요.
                3. 핵심 개념을 먼저 설명하세요.
                4. 필요한 경우 예시를 포함하세요.
                5. 중복된 설명은 제거하세요.
                6. 마크다운 형식으로 작성하세요.
                """;
    }

    public String buildUserPrompt(
            TopicDocumentContext context
    ) {
        return """
                다음 Topic에 대한 개념 설명을 작성하세요.

                [Topic ID]
                %d

                [Topic 제목]
                %s

                [타입별 참고 문서]
                %s

                [출력 형식]
                제목: Topic 제목
                내용: 학습용 개념 설명
                """.formatted(
                context.topicId(),
                context.topicTitle(),
                buildTypedChunkContext(context)
        );
    }

    private String buildTypedChunkContext(TopicDocumentContext context) {
        StringBuilder builder = new StringBuilder();

        for (TopicDocumentChunk chunk : context.chunks()) {
            builder.append("\n--- 청크 ")
                    .append(chunk.chunkOrder())
                    .append(" / 타입: ")
                    .append(chunk.contentType())
                    .append(" ---\n")
                    .append("[작성 지침]\n")
                    .append(chunkTypePromptRouter.instructionFor(chunk.contentType()))
                    .append("\n[원문]\n")
                    .append(chunk.contentText())
                    .append('\n');
        }

        return builder.toString();
    }
}
