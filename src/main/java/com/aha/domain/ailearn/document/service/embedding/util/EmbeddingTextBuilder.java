package com.aha.domain.ailearn.document.service.embedding.util;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.exam.entity.ExamScopeNode;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingTextBuilder {

    public String buildChunkEmbeddingText(DocumentChunk chunk) {
        StringBuilder builder = new StringBuilder();

        appendIfNotBlank(builder, "섹션 제목", chunk.getSectionTitle());
        appendIfNotBlank(builder, "목차 경로", chunk.getHeadingPath());

        if (chunk.getContentType() != null) {
            appendIfNotBlank(builder, "청크 타입", chunk.getContentType().name());
        }

        if (chunk.getCodeLanguage() != null) {
            appendIfNotBlank(builder, "코드 언어", chunk.getCodeLanguage().name());
        }

        appendIfNotBlank(builder, "본문", chunk.getContentText());

        return builder.toString().trim();
    }

    public String buildScopeNodeEmbeddingText(ExamScopeNode scopeNode) {
        StringBuilder builder = new StringBuilder();

        appendIfNotBlank(builder, "목차 코드", scopeNode.getCode());
        appendIfNotBlank(builder, "목차 제목", scopeNode.getTitle());
        appendIfNotBlank(builder, "목차 설명", scopeNode.getDescription());
        appendIfNotBlank(builder, "키워드", scopeNode.getKeywordsJson());

        return builder.toString().trim();
    }

    private void appendIfNotBlank(StringBuilder builder, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        builder.append(label)
                .append(": ")
                .append(value.trim())
                .append("\n");
    }
}