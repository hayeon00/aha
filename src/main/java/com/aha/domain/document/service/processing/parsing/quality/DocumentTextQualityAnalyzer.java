package com.aha.domain.document.service.processing.parsing.quality;

import org.springframework.stereotype.Component;

@Component
public class DocumentTextQualityAnalyzer {

    private static final double MIN_ACCEPTABLE_READABLE_RATIO = 0.72;

    public TextQuality analyze(String text) {
        if (text == null || text.isBlank()) {
            return new TextQuality(0, 0, 0, 0, 0.0, 0.0);
        }

        int compactLength = 0;
        int readableCount = 0;
        int meaningfulCount = 0;
        int replacementCount = 0;
        int longestRepeatedRun = 1;
        int currentRepeatedRun = 1;
        int previous = -1;

        for (int index = 0; index < text.length(); index++) {
            int character = text.charAt(index);

            if (!Character.isWhitespace(character)) {
                compactLength++;
                if (isReadable(character)) readableCount++;
                if (Character.isLetterOrDigit(character) || isKorean(character)) meaningfulCount++;
                if (character == '\uFFFD' || character == '\u25A1') replacementCount++;
            }

            if (character == previous && !Character.isWhitespace(character)) {
                currentRepeatedRun++;
                longestRepeatedRun = Math.max(longestRepeatedRun, currentRepeatedRun);
            } else {
                currentRepeatedRun = 1;
                previous = character;
            }
        }

        if (compactLength == 0) {
            return new TextQuality(text.length(), 0, 0, 0, 0.0, 0.0);
        }

        double readableRatio = (double) readableCount / compactLength;
        double replacementRatio = (double) replacementCount / compactLength;
        double lengthScore = Math.min(1.0, meaningfulCount / 80.0);
        double repetitionPenalty = longestRepeatedRun >= 8 ? 0.20 : 0.0;
        double score = clamp(
                readableRatio * 0.65
                        + lengthScore * 0.30
                        + (1.0 - Math.min(1.0, replacementRatio * 10)) * 0.05
                        - repetitionPenalty
        );

        return new TextQuality(
                text.length(),
                compactLength,
                meaningfulCount,
                longestRepeatedRun,
                readableRatio,
                score
        );
    }

    public boolean isAcceptable(String text) {
        TextQuality quality = analyze(text);
        return quality.meaningfulCharacterCount() >= 20
                && quality.readableRatio() >= MIN_ACCEPTABLE_READABLE_RATIO
                && quality.longestRepeatedRun() < 12;
    }

    private boolean isReadable(int character) {
        return Character.isLetterOrDigit(character)
                || isKorean(character)
                || ".,!?()[]{}:;+-=*/%_'\"@#&<>|~`·ㆍ…“”‘’\u00B7".indexOf(character) >= 0;
    }

    private boolean isKorean(int character) {
        return (character >= 0xAC00 && character <= 0xD7A3)
                || (character >= 0x3131 && character <= 0x318E)
                || (character >= 0x1100 && character <= 0x11FF);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public record TextQuality(
            int rawLength,
            int compactLength,
            int meaningfulCharacterCount,
            int longestRepeatedRun,
            double readableRatio,
            double score
    ) {
    }
}
