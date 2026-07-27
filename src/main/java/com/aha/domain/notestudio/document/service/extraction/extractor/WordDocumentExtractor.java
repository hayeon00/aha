package com.aha.domain.notestudio.document.service.extraction.extractor;

import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;
import com.aha.domain.notestudio.document.service.extraction.model.DocumentBlock;
import com.aha.domain.notestudio.document.service.extraction.model.ExtractedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class WordDocumentExtractor implements DocumentExtractor {

    @Override
    public boolean supports(String extension) {
        return "docx".equalsIgnoreCase(extension);
    }

    @Override
    public ExtractedDocument extract(SourceDocument sourceDocument, Path documentPath) {
        try (InputStream inputStream = Files.newInputStream(documentPath);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            List<DocumentBlock> blocks = new ArrayList<>();
            StringBuilder fullTextBuilder = new StringBuilder();

            String currentHeadingPath = null;
            String currentSectionTitle = null;

            for (IBodyElement bodyElement : document.getBodyElements()) {
                if (bodyElement.getElementType() == BodyElementType.PARAGRAPH) {
                    XWPFParagraph paragraph = (XWPFParagraph) bodyElement;

                    String paragraphText = normalizeText(paragraph.getText());

                    if (paragraphText == null) {
                        continue;
                    }

                    boolean heading = looksLikeHeadingParagraph(paragraph, paragraphText);

                    if (heading) {
                        currentSectionTitle = paragraphText;
                        currentHeadingPath = appendHeadingPath(
                                currentHeadingPath,
                                paragraphText
                        );

                        DocumentBlock headingBlock = new DocumentBlock(
                                null,
                                currentHeadingPath,
                                currentSectionTitle,
                                DocumentChunkContentType.HEADING,
                                null,
                                paragraphText,
                                paragraphText
                        );

                        blocks.add(headingBlock);
                        appendFullText(fullTextBuilder, paragraphText);
                        continue;
                    }

                    DocumentBlock textBlock = new DocumentBlock(
                            null,
                            currentHeadingPath,
                            currentSectionTitle,
                            DocumentChunkContentType.TEXT,
                            null,
                            paragraphText,
                            paragraphText
                    );

                    blocks.add(textBlock);
                    appendFullText(fullTextBuilder, paragraphText);
                    continue;
                }

                if (bodyElement.getElementType() == BodyElementType.TABLE) {
                    XWPFTable table = (XWPFTable) bodyElement;

                    String tableText = extractTableText(table);

                    if (tableText == null || tableText.isBlank()) {
                        continue;
                    }

                    DocumentBlock tableBlock = new DocumentBlock(
                            null,
                            currentHeadingPath,
                            currentSectionTitle,
                            DocumentChunkContentType.TABLE,
                            null,
                            tableText,
                            tableText
                    );

                    blocks.add(tableBlock);
                    appendFullText(fullTextBuilder, tableText);
                }
            }

            String fullText = normalizeText(fullTextBuilder.toString());

            validateExtractedResult(
                    sourceDocument,
                    fullText,
                    blocks
            );

            return ExtractedDocument.of(
                    fullText,
                    blocks
            );

        } catch (BusinessException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "DOCX 문서 텍스트 추출 실패. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }
    }

    private boolean looksLikeHeadingParagraph(
            XWPFParagraph paragraph,
            String paragraphText
    ) {
        if (paragraphText == null || paragraphText.isBlank()) {
            return false;
        }

        String style = paragraph.getStyle();

        if (style != null && !style.isBlank()) {
            String normalizedStyle = style.toLowerCase(Locale.ROOT);

            if (normalizedStyle.startsWith("heading")
                    || normalizedStyle.startsWith("title")
                    || normalizedStyle.contains("heading")
                    || normalizedStyle.contains("제목")) {
                return true;
            }
        }

        return looksLikeHeadingText(paragraphText);
    }

    private boolean looksLikeHeadingText(String text) {
        String normalizedText = text.trim();

        if (normalizedText.length() > 80) {
            return false;
        }

        if (normalizedText.matches("^\\s*(\\d+)(\\.\\d+)*[.)]?\\s+.{2,}$")) {
            return true;
        }

        if (normalizedText.matches("^\\s*(제\\s*\\d+\\s*[장절항]|\\d+\\s*[장절항])\\s+.{2,}$")) {
            return true;
        }

        return isShortKeywordHeading(normalizedText);
    }

    private boolean isShortKeywordHeading(String text) {
        if (text.length() > 30) {
            return false;
        }

        String upperText = text.toUpperCase(Locale.ROOT);

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

    private String extractTableText(XWPFTable table) {
        List<String> rowTexts = new ArrayList<>();

        for (XWPFTableRow row : table.getRows()) {
            List<String> cellTexts = new ArrayList<>();

            for (XWPFTableCell cell : row.getTableCells()) {
                String cellText = normalizeText(cell.getText());

                if (cellText == null) {
                    cellText = "";
                }

                cellTexts.add(cellText.replace("\n", " "));
            }

            String rowText = String.join(" | ", cellTexts).trim();

            if (!rowText.isBlank()) {
                rowTexts.add(rowText);
            }
        }

        if (rowTexts.isEmpty()) {
            return null;
        }

        return String.join("\n", rowTexts);
    }

    private String appendHeadingPath(
            String currentHeadingPath,
            String heading
    ) {
        if (heading == null || heading.isBlank()) {
            return currentHeadingPath;
        }

        String normalizedHeading = heading.trim();

        if (currentHeadingPath == null || currentHeadingPath.isBlank()) {
            return normalizedHeading;
        }

        if (currentHeadingPath.endsWith(normalizedHeading)) {
            return currentHeadingPath;
        }

        return currentHeadingPath + " > " + normalizedHeading;
    }

    private void appendFullText(
            StringBuilder fullTextBuilder,
            String text
    ) {
        if (text == null || text.isBlank()) {
            return;
        }

        if (!fullTextBuilder.isEmpty()) {
            fullTextBuilder.append("\n\n");
        }

        fullTextBuilder.append(text.trim());
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String normalizedText = text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        if (normalizedText.isBlank()) {
            return null;
        }

        return normalizedText;
    }

    private void validateExtractedResult(
            SourceDocument sourceDocument,
            String fullText,
            List<DocumentBlock> blocks
    ) {
        if (fullText == null || fullText.isBlank() || blocks == null || blocks.isEmpty()) {
            log.warn(
                    "DOCX 문서에서 텍스트를 추출하지 못했습니다. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName()
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }
    }
}