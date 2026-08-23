package com.aha.domain.document.service.processing.embedding.util;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.exam.entity.ExamScopeNode;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingTextBuilder {

    /**
     * Chunk embedding 입력 텍스트.
     *
     * 원칙:
     * 1. sectionTitle은 의미 힌트로 1회만 포함한다.
     * 2. contentText는 이미 "제목 + 본문"일 수 있으므로 중복 사용하지 않는다.
     * 3. 실제 의미 내용은 rawText를 우선 사용한다.
     * 4. headingPath가 sectionTitle과 다르고 실제 계층 정보를 가질 때만 포함한다.
     * 5. contentType, codeLanguage 같은 메타데이터는 Topic 의미 판별에는
     *    기여도가 낮아 embedding 입력에서 제외한다.
     */
    public String buildChunkEmbeddingText(
            DocumentChunk chunk
    ) {
        if (chunk == null) {
            return "";
        }

        StringBuilder builder =
                new StringBuilder();

        String sectionTitle =
                normalize(chunk.getSectionTitle());

        String headingPath =
                normalize(chunk.getHeadingPath());

        String bodyText =
                resolveChunkBodyText(
                        chunk,
                        sectionTitle
                );

        appendIfNotBlank(
                builder,
                "주제",
                sectionTitle
        );

        /*
         * 현재 Parser에서는 headingPath가 sectionTitle과 같은 경우가 많다.
         * 추후 "데이터 모델링 > 정규화"처럼 계층 경로가 들어오면
         * 의미 있는 추가 context로 사용할 수 있다.
         */
        if (headingPath != null
                && !headingPath.equalsIgnoreCase(
                sectionTitle == null
                        ? ""
                        : sectionTitle
        )) {

            appendIfNotBlank(
                    builder,
                    "상위 목차",
                    headingPath
            );
        }

        appendIfNotBlank(
                builder,
                "내용",
                bodyText
        );

        return builder
                .toString()
                .trim();
    }

    /**
     * 시험 Topic embedding 입력 텍스트.
     *
     * Topic을 단순 title 한 단어로만 표현하지 않고
     * description + keywords를 함께 사용해 semantic retrieval의 recall을 높인다.
     *
     * code는 식별자 성격이 강하므로 embedding 의미 공간에는 넣지 않는다.
     */
    public String buildScopeNodeEmbeddingText(
            ExamScopeNode scopeNode
    ) {
        if (scopeNode == null) {
            return "";
        }

        StringBuilder builder =
                new StringBuilder();

        appendIfNotBlank(
                builder,
                "주제",
                scopeNode.getTitle()
        );

        appendIfNotBlank(
                builder,
                "설명",
                scopeNode.getDescription()
        );

        appendIfNotBlank(
                builder,
                "관련 키워드",
                normalizeKeywords(
                        scopeNode.getKeywordsJson()
                )
        );

        return builder
                .toString()
                .trim();
    }

    /**
     * rawText는 현재 청크가 실제 담당하는 본문이므로 우선 사용한다.
     *
     * 이전 데이터나 예외적인 청크에서 rawText가 없다면
     * contentText를 fallback으로 사용하되,
     * 앞에 이미 포함된 sectionTitle은 제거해 제목 중복을 방지한다.
     */
    private String resolveChunkBodyText(
            DocumentChunk chunk,
            String sectionTitle
    ) {
        String rawText =
                normalize(
                        chunk.getRawText()
                );

        if (rawText != null) {
            return rawText;
        }

        String contentText =
                normalize(
                        chunk.getContentText()
                );

        if (contentText == null) {
            return null;
        }

        if (sectionTitle != null
                && contentText.startsWith(
                sectionTitle
        )) {

            String remainder =
                    contentText.substring(
                                    sectionTitle.length()
                            )
                            .trim();

            if (!remainder.isBlank()) {
                return remainder;
            }
        }

        return contentText;
    }

    /**
     * keywordsJson이 단순 JSON 배열 문자열 형태라면
     * embedding에는 JSON 문법 자체가 필요하지 않으므로
     * 최소한의 구분자 정리만 수행한다.
     *
     * 예:
     * ["함수 종속", "제3정규형"]
     * -> 함수 종속, 제3정규형
     */
    private String normalizeKeywords(
            String keywords
    ) {
        String normalized =
                normalize(keywords);

        if (normalized == null) {
            return null;
        }

        return normalized
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replaceAll("\\s*,\\s*", ", ")
                .trim();
    }

    private void appendIfNotBlank(
            StringBuilder builder,
            String label,
            String value
    ) {
        String normalized =
                normalize(value);

        if (normalized == null) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append('\n');
        }

        builder.append(label)
                .append(": ")
                .append(normalized);
    }

    private String normalize(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        String normalized =
                value.replace("\r\n", "\n")
                        .replace('\r', '\n')
                        .replace('\u00A0', ' ')
                        .replaceAll("[\\t ]+", " ")
                        .replaceAll("\\n{3,}", "\n\n")
                        .trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}