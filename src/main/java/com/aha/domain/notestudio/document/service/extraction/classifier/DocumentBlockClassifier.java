package com.aha.domain.notestudio.document.service.extraction.classifier;

import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;
import com.aha.domain.notestudio.document.service.extraction.model.DocumentBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentBlockClassifier {

    private final List<DocumentBlockTypeDetector> detectors;

    public List<DocumentBlock> classify(List<DocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        List<DocumentBlock> classifiedBlocks = new ArrayList<>();

        String currentHeadingPath = null;
        String currentSectionTitle = null;

        for (DocumentBlock block : blocks) {
            if (block == null || block.isBlank()) {
                continue;
            }

            if (shouldPreserveAsSingleUnit(block)) {
                classifiedBlocks.add(normalizePreservedBlock(block));
                continue;
            }

            List<String> units = splitBlockIntoUnits(block.text());

            for (String unit : units) {
                String text = normalizeUnit(unit);

                if (!isMeaningfulUnit(text)) {
                    continue;
                }

                ClassifiedBlockType classifiedBlockType = classifyText(text);

                if (classifiedBlockType.contentType() == DocumentChunkContentType.HEADING) {
                    currentSectionTitle = text;
                    currentHeadingPath = appendHeadingPath(
                            currentHeadingPath,
                            text
                    );

                    classifiedBlocks.add(new DocumentBlock(
                            block.pageNo(),
                            currentHeadingPath,
                            currentSectionTitle,
                            DocumentChunkContentType.HEADING,
                            null,
                            text,
                            unit
                    ));

                    continue;
                }

                classifiedBlocks.add(new DocumentBlock(
                        block.pageNo(),
                        currentHeadingPath,
                        currentSectionTitle,
                        classifiedBlockType.contentType(),
                        classifiedBlockType.codeLanguage(),
                        text,
                        unit
                ));
            }
        }

        return List.copyOf(classifiedBlocks);
    }

    private boolean shouldPreserveAsSingleUnit(DocumentBlock block) {
        if (block == null || block.contentType() == null) {
            return false;
        }

        return block.contentType() == DocumentChunkContentType.TABLE
                || block.contentType() == DocumentChunkContentType.CODE
                || block.contentType() == DocumentChunkContentType.COMMAND
                || block.contentType() == DocumentChunkContentType.CONFIG
                || block.contentType() == DocumentChunkContentType.FORMULA;
    }

    private DocumentBlock normalizePreservedBlock(DocumentBlock block) {
        String text = normalizeUnit(block.text());
        String rawText = block.rawText() == null || block.rawText().isBlank()
                ? text
                : block.rawText();

        return new DocumentBlock(
                block.pageNo(),
                normalizeNullableText(block.headingPath()),
                normalizeNullableText(block.sectionTitle()),
                block.contentType(),
                block.resolvedCodeLanguage(),
                text,
                rawText
        );
    }

    private String normalizeNullableText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return normalizeUnit(text);
    }

    private List<String> splitBlockIntoUnits(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();

        String[] paragraphs = normalizedText.split("\\n\\s*\\n");

        List<String> units = new ArrayList<>();

        for (String paragraph : paragraphs) {
            String trimmedParagraph = normalizeUnit(paragraph);

            if (!isMeaningfulUnit(trimmedParagraph)) {
                continue;
            }

            if (isSpecialBlock(trimmedParagraph)) {
                units.add(trimmedParagraph);
                continue;
            }

            units.addAll(splitParagraphBySpecialLines(trimmedParagraph));
        }

        return List.copyOf(units);
    }

    private List<String> splitParagraphBySpecialLines(String paragraph) {
        String[] lines = paragraph.split("\\n");

        List<String> units = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = normalizeUnit(line);

            if (!isMeaningfulUnit(trimmedLine)) {
                continue;
            }

            if (isSpecialBlock(trimmedLine)) {
                flushCurrentText(units, currentText);
                units.add(trimmedLine);
                continue;
            }

            if (!currentText.isEmpty()) {
                currentText.append("\n");
            }

            currentText.append(trimmedLine);
        }

        flushCurrentText(units, currentText);

        return List.copyOf(units);
    }

    private boolean isSpecialBlock(String text) {
        ClassifiedBlockType classifiedBlockType = classifyText(text);

        return classifiedBlockType.contentType() == DocumentChunkContentType.HEADING
                || classifiedBlockType.contentType() == DocumentChunkContentType.TABLE
                || classifiedBlockType.contentType() == DocumentChunkContentType.CODE
                || classifiedBlockType.contentType() == DocumentChunkContentType.FORMULA
                || classifiedBlockType.contentType() == DocumentChunkContentType.COMMAND
                || classifiedBlockType.contentType() == DocumentChunkContentType.CONFIG;
    }

    private ClassifiedBlockType classifyText(String text) {
        return detectors.stream()
                .sorted(Comparator.comparingInt(DocumentBlockTypeDetector::order))
                .filter(detector -> detector.supports(text))
                .findFirst()
                .map(detector -> detector.detect(text))
                .orElse(ClassifiedBlockType.text());
    }

    private void flushCurrentText(
            List<String> units,
            StringBuilder currentText
    ) {
        if (currentText.isEmpty()) {
            return;
        }

        String text = normalizeUnit(currentText.toString());

        if (isMeaningfulUnit(text)) {
            units.add(text);
        }

        currentText.setLength(0);
    }

    private boolean isMeaningfulUnit(String text) {
        return text != null && !text.isBlank();
    }

    private String appendHeadingPath(
            String currentHeadingPath,
            String heading
    ) {
        if (heading == null || heading.isBlank()) {
            return currentHeadingPath;
        }

        if (currentHeadingPath == null || currentHeadingPath.isBlank()) {
            return heading.trim();
        }

        String normalizedHeading = heading.trim();

        if (currentHeadingPath.endsWith(normalizedHeading)) {
            return currentHeadingPath;
        }

        return currentHeadingPath + " > " + normalizedHeading;
    }

    private String normalizeUnit(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}