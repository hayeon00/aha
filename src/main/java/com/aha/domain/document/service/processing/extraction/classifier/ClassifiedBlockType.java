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
        DocumentChunkCodeLanguage codeLanguage,
        boolean headingBlock
) {
    public static ClassifiedBlockType text() {
        return new ClassifiedBlockType(DocumentChunkContentType.TEXT, null, false);
    }

    public static ClassifiedBlockType heading() {
        return new ClassifiedBlockType(DocumentChunkContentType.TEXT, null, true);
    }

    public static ClassifiedBlockType table() {
        return new ClassifiedBlockType(DocumentChunkContentType.TABLE, null, false);
    }

    public static ClassifiedBlockType sqlCode() {
        return new ClassifiedBlockType(
                DocumentChunkContentType.CODE,
                DocumentChunkCodeLanguage.SQL,
                false
        );
    }
}
