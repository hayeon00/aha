package com.aha.domain.ailearn.document.client.content;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LearningContentContextBuilder
 * @since : 2026. 6. 25. 목요일
 */

@Component
public class LearningContentContextBuilder {

    public String build(List<DocumentChunk> chunks){

        if(chunks==null || chunks.isEmpty()) return "";

        return chunks.stream()
                .map(this::formatChunk)
                .collect(Collectors.joining("\n\n"));
    }

    private String formatChunk(DocumentChunk chunk) {

        return """
                [문서 청크]
                청크 ID: %d
                페이지: %s
                섹션 제목: %s
                내용:
                %s
                """.formatted(
                chunk.getId(),
                resolvePageNo(chunk),
                resolveSectionTitle(chunk),
                chunk.getContentText()
        );

    }

    private String resolvePageNo(DocumentChunk chunk) {

        if(chunk.getPageNo() == null) return "알 수 없음";

        return String.valueOf(chunk.getPageNo());

    }

    private String resolveSectionTitle(DocumentChunk chunk) {

        if(chunk.getSectionTitle() == null || chunk.getSectionTitle().isBlank()) return "없음";

        return  chunk.getSectionTitle();

    }


}
