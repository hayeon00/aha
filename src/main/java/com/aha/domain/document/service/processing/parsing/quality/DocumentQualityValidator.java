package com.aha.domain.document.service.processing.parsing.quality;

import com.aha.domain.document.service.processing.parsing.model.DocumentBlock;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentQualityValidator {

    private static final int MIN_TOTAL_TEXT_LENGTH = 50;
    private static final int MIN_MEANINGFUL_BLOCK_LENGTH = 20;
    private static final double MIN_DOCUMENT_READABLE_RATIO = 0.70;

    private final DocumentTextQualityAnalyzer qualityAnalyzer;

    public void validate(
            ParsedDocument document
    ) {
        if (document == null
                || document.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }

        if (document.totalTextLength()
                < MIN_TOTAL_TEXT_LENGTH) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }

        boolean hasMeaningfulBlock =
                document.blocks()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(DocumentBlock::text)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .anyMatch(text ->
                                text.length()
                                        >= MIN_MEANINGFUL_BLOCK_LENGTH
                        );

        if (!hasMeaningfulBlock) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }

        String combinedText = document.blocks().stream()
                .filter(Objects::nonNull)
                .map(DocumentBlock::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        DocumentTextQualityAnalyzer.TextQuality quality = qualityAnalyzer.analyze(combinedText);
        if (quality.readableRatio() < MIN_DOCUMENT_READABLE_RATIO
                || quality.meaningfulCharacterCount() < MIN_TOTAL_TEXT_LENGTH
                || quality.longestRepeatedRun() >= 20) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }
}
