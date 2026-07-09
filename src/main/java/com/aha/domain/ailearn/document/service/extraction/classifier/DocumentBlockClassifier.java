package com.aha.domain.ailearn.document.service.extraction.classifier;

import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import com.aha.domain.ailearn.document.service.extraction.model.DocumentBlock;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DocumentBlockClassifier {

    private static final int MAX_HEADING_LENGTH = 80;

    private static final Pattern NUMBERED_HEADING_PATTERN = Pattern.compile(
            "^\\s*(\\d+)(\\.\\d+)*[.)]?\\s+.{2,}$"
    );

    private static final Pattern KOREAN_HEADING_PATTERN = Pattern.compile(
            "^\\s*(제\\s*\\d+\\s*[장절항]|\\d+\\s*[장절항])\\s+.{2,}$"
    );

    private static final Pattern SQL_START_PATTERN = Pattern.compile(
            "^(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP|TRUNCATE|MERGE|WITH|COMMIT|ROLLBACK)\\b.*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_KEYWORD_PATTERN = Pattern.compile(
            "\\b(SELECT|FROM|WHERE|GROUP\\s+BY|ORDER\\s+BY|HAVING|JOIN|INNER\\s+JOIN|LEFT\\s+JOIN|RIGHT\\s+JOIN|ON|INSERT\\s+INTO|VALUES|UPDATE|SET|DELETE\\s+FROM|CREATE\\s+TABLE|ALTER\\s+TABLE|COMMIT|ROLLBACK|TRANSACTION|NULL|IS\\s+NULL|IS\\s+NOT\\s+NULL|PRIMARY\\s+KEY|FOREIGN\\s+KEY)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SYMBOL_ONLY_PATTERN = Pattern.compile(
            "^[\\p{Punct}\\p{So}\\s]+$"
    );

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

            List<String> units = splitBlockIntoUnits(block.text());

            for (String unit : units) {
                String text = normalizeUnit(unit);

                if (!isMeaningfulUnit(text)) {
                    continue;
                }

                DocumentChunkContentType contentType = classifyText(text);

                if (contentType == DocumentChunkContentType.HEADING) {
                    currentSectionTitle = text;
                    currentHeadingPath = appendHeadingPath(currentHeadingPath, text);

                    classifiedBlocks.add(new DocumentBlock(
                            block.pageNo(),
                            currentHeadingPath,
                            currentSectionTitle,
                            DocumentChunkContentType.HEADING,
                            text,
                            unit
                    ));

                    continue;
                }

                classifiedBlocks.add(new DocumentBlock(
                        block.pageNo(),
                        currentHeadingPath,
                        currentSectionTitle,
                        contentType,
                        text,
                        unit
                ));
            }
        }

        return List.copyOf(classifiedBlocks);
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

            if (looksLikeSqlBlock(trimmedParagraph) || looksLikeTable(trimmedParagraph)) {
                units.add(trimmedParagraph);
                continue;
            }

            units.addAll(splitParagraphByHeadingLines(trimmedParagraph));
        }

        return List.copyOf(units);
    }

    private List<String> splitParagraphByHeadingLines(String paragraph) {
        String[] lines = paragraph.split("\\n");

        List<String> units = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = normalizeUnit(line);

            if (!isMeaningfulUnit(trimmedLine)) {
                continue;
            }

            if (looksLikeHeading(trimmedLine)) {
                flushCurrentText(units, currentText);
                units.add(trimmedLine);
                continue;
            }

            if (looksLikeSqlBlock(trimmedLine) || looksLikeTable(trimmedLine)) {
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

    private void flushCurrentText(List<String> units, StringBuilder currentText) {
        if (currentText.isEmpty()) {
            return;
        }

        String text = normalizeUnit(currentText.toString());

        if (isMeaningfulUnit(text)) {
            units.add(text);
        }

        currentText.setLength(0);
    }

    private DocumentChunkContentType classifyText(String text) {
        if (looksLikeSqlBlock(text)) {
            return DocumentChunkContentType.SQL;
        }

        if (looksLikeTable(text)) {
            return DocumentChunkContentType.TABLE;
        }

        if (looksLikeHeading(text)) {
            return DocumentChunkContentType.HEADING;
        }

        return DocumentChunkContentType.TEXT;
    }

    private boolean isMeaningfulUnit(String text) {
        return text != null && !text.isBlank();
    }

    private boolean isImportantShortKeyword(String text) {
        String upperText = text.toUpperCase();

        return upperText.equals("SELECT")
                || upperText.equals("FROM")
                || upperText.equals("WHERE")
                || upperText.equals("GROUP BY")
                || upperText.equals("ORDER BY")
                || upperText.equals("HAVING")
                || upperText.equals("JOIN")
                || upperText.equals("NULL")
                || upperText.equals("IS NULL")
                || upperText.equals("COMMIT")
                || upperText.equals("ROLLBACK")
                || upperText.equals("DDL")
                || upperText.equals("DML")
                || upperText.equals("DCL")
                || upperText.equals("TCL");
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

    private boolean looksLikeSqlBlock(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalizedText = text.trim();

        if (normalizedText.length() < 6) {
            return false;
        }

        String[] lines = normalizedText.split("\\n");

        int sqlKeywordCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (SQL_START_PATTERN.matcher(trimmedLine).matches()) {
                sqlKeywordCount++;
            }

            if (SQL_KEYWORD_PATTERN.matcher(trimmedLine).find()) {
                sqlKeywordCount++;
            }
        }

        long keywordCount = SQL_KEYWORD_PATTERN.matcher(normalizedText).results().count();

        boolean hasSemicolon = normalizedText.contains(";");
        boolean hasMultipleSqlKeywords = keywordCount >= 2;
        boolean shortSqlKeywordGroup = keywordCount >= 1 && normalizedText.length() <= 80;

        return sqlKeywordCount >= 2
                || hasSemicolon && hasMultipleSqlKeywords
                || shortSqlKeywordGroup;
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

    private String appendHeadingPath(String currentHeadingPath, String heading) {
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