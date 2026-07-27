package com.aha.domain.notestudio.document.service.extraction.classifier.detector;

import com.aha.domain.notestudio.document.service.extraction.classifier.ClassifiedBlockType;
import com.aha.domain.notestudio.document.service.extraction.classifier.DocumentBlockTypeDetector;
import org.springframework.stereotype.Component;

@Component
public class TableBlockDetector implements DocumentBlockTypeDetector {

    @Override
    public boolean supports(String text) {
        return looksLikeTable(text);
    }

    @Override
    public ClassifiedBlockType detect(String text) {
        return ClassifiedBlockType.table();
    }

    @Override
    public int order() {
        return 10;
    }

    private boolean looksLikeTable(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalizedText = text.trim();

        if (normalizedText.contains("|")) {
            String[] lines = normalizedText.split("\\n");
            int tableLineCount = 0;

            for (String line : lines) {
                if (line.chars().filter(ch -> ch == '|').count() >= 2) {
                    tableLineCount++;
                }
            }

            if (tableLineCount >= 2) {
                return true;
            }
        }

        String[] lines = normalizedText.split("\\n");

        if (lines.length < 2) {
            return false;
        }

        int alignedLineCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.split("\\s{2,}").length >= 3) {
                alignedLineCount++;
            }
        }

        return alignedLineCount >= 2;
    }
}