package com.aha.domain.ailearn.document.service.extraction.classifier.detector;

import com.aha.domain.ailearn.document.service.extraction.classifier.ClassifiedBlockType;
import com.aha.domain.ailearn.document.service.extraction.classifier.DocumentBlockTypeDetector;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class HeadingBlockDetector implements DocumentBlockTypeDetector {

    private static final int MAX_HEADING_LENGTH = 80;

    private static final Pattern NUMBERED_HEADING_PATTERN = Pattern.compile(
            "^\\s*(\\d+)(\\.\\d+)*[.)]?\\s+.{2,}$"
    );

    private static final Pattern KOREAN_HEADING_PATTERN = Pattern.compile(
            "^\\s*(제\\s*\\d+\\s*[장절항]|\\d+\\s*[장절항])\\s+.{2,}$"
    );

    @Override
    public boolean supports(String text) {
        return looksLikeHeading(text);
    }

    @Override
    public ClassifiedBlockType detect(String text) {
        return ClassifiedBlockType.heading();
    }

    @Override
    public int order() {
        return 20;
    }

    private boolean looksLikeHeading(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalizedText = text.trim();

        if (normalizedText.length() > MAX_HEADING_LENGTH) {
            return false;
        }

        if (normalizedText.contains(".")
                && normalizedText.length() > 40
                && normalizedText.endsWith(".")) {
            return false;
        }

        if (NUMBERED_HEADING_PATTERN.matcher(normalizedText).matches()) {
            return true;
        }

        if (KOREAN_HEADING_PATTERN.matcher(normalizedText).matches()) {
            return true;
        }

        return isShortKeywordHeading(normalizedText);
    }

    private boolean isShortKeywordHeading(String text) {
        if (text.length() > 30) {
            return false;
        }

        String upperText = text.toUpperCase();

        return upperText.equals("SELECT")
                || upperText.equals("WHERE")
                || upperText.equals("GROUP BY")
                || upperText.equals("HAVING")
                || upperText.equals("ORDER BY")
                || upperText.equals("JOIN")
                || upperText.equals("SUBQUERY")
                || upperText.equals("DDL")
                || upperText.equals("DML")
                || upperText.equals("DCL")
                || upperText.equals("TCL")
                || text.endsWith("개요")
                || text.endsWith("정의")
                || text.endsWith("특징")
                || text.endsWith("종류")
                || text.endsWith("문법")
                || text.endsWith("예시");
    }
}