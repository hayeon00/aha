package com.aha.domain.document.service.processing.parsing.quality;

import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.service.processing.parsing.model.DocumentBlock;
import com.aha.domain.document.service.processing.parsing.model.DocumentExtractionMethod;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentQualityValidatorTest {

    private final DocumentQualityValidator validator = new DocumentQualityValidator(
            new DocumentTextQualityAnalyzer()
    );

    @Test
    void acceptsMeaningfulParsedDocument() {
        ParsedDocument document = parsedDocument(
                "엔터티는 업무에서 관리해야 하는 데이터의 집합이며 식별자와 속성을 가집니다. "
                        + "관계는 엔터티 사이의 연관성을 표현합니다."
        );

        assertThatCode(() -> validator.validate(document)).doesNotThrowAnyException();
    }

    @Test
    void rejectsLongButBrokenExtractionResult() {
        ParsedDocument document = parsedDocument(
                "□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□"
        );

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(BusinessException.class);
    }

    private ParsedDocument parsedDocument(String text) {
        return new ParsedDocument(List.of(new DocumentBlock(
                1,
                null,
                null,
                null,
                DocumentChunkContentType.TEXT,
                null,
                text,
                text,
                DocumentExtractionMethod.NATIVE
        )));
    }
}
