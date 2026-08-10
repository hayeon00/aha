package com.aha.domain.document.service.processing.extraction.classifier;

import com.aha.domain.document.enums.DocumentChunkCodeLanguage;
import com.aha.domain.document.enums.DocumentChunkContentType;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ClassifiedBlockType
 * @since : 2026. 7. 9. 목요일
 */

public record ClassifiedBlockType(
        DocumentChunkContentType contentType,
        DocumentChunkCodeLanguage codeLanguage
) {
    public static ClassifiedBlockType text() {
        return new ClassifiedBlockType(DocumentChunkContentType.TEXT, null);
    }

    public static ClassifiedBlockType heading() {
        return new ClassifiedBlockType(DocumentChunkContentType.HEADING, null);
    }

    public static ClassifiedBlockType table() {
        return new ClassifiedBlockType(DocumentChunkContentType.TABLE, null);
    }

    public static ClassifiedBlockType sqlCode() {
        return new ClassifiedBlockType(
                DocumentChunkContentType.CODE,
                DocumentChunkCodeLanguage.SQL
        );
    }
}
