package com.aha.domain.document.service.processing.parsing.model;

import com.aha.domain.document.enums.DocumentChunkContentType;

public record DocumentBlock(

        Integer pageNo,

        Integer headingLevel,

        String headingPath,

        String sectionTitle,

        DocumentChunkContentType contentType,

        String codeLanguage,

        String text,

        String rawText,

        DocumentExtractionMethod extractionMethod

) {

    public boolean isBlank() {
        return text == null
                || text.isBlank();
    }

    public boolean isHeading() {
        return headingLevel != null
                && headingLevel > 0;
    }

    public DocumentExtractionMethod resolvedExtractionMethod() {
        return extractionMethod == null
                ? DocumentExtractionMethod.NATIVE
                : extractionMethod;
    }
}
