package com.aha.domain.document.service.processing.parsing.quality;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTextQualityAnalyzerTest {

    private final DocumentTextQualityAnalyzer analyzer = new DocumentTextQualityAnalyzer();

    @Test
    void acceptsReadableKoreanAndEnglishLearningText() {
        String text = "엔터티는 업무에서 관리해야 하는 데이터의 집합입니다. "
                + "An entity has attributes and a stable identifier.";

        assertThat(analyzer.isAcceptable(text)).isTrue();
        assertThat(analyzer.analyze(text).readableRatio()).isGreaterThan(0.9);
    }

    @Test
    void rejectsBrokenOrRepeatedExtractionResult() {
        String text = "□□□□□□□□□□□□ %%%%%%%% ������������";

        assertThat(analyzer.isAcceptable(text)).isFalse();
        assertThat(analyzer.analyze(text).score()).isLessThan(0.5);
    }
}
