package com.aha.domain.document.service.processing.parsing.parser;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.service.processing.parsing.model.DocumentBlock;
import com.aha.domain.document.service.processing.parsing.model.DocumentExtractionMethod;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DocxDocumentParser
        implements DocumentParser {

    private static final Pattern HEADING_STYLE_PATTERN =
            Pattern.compile(
                    "(?i).*(?:heading|제목)\\s*(\\d+).*"
            );
    private static final Pattern TITLE_STYLE_PATTERN =
            Pattern.compile("(?i)^(?:title|제목)$");

    @Override
    public boolean supports(
            DocumentFileExtension fileExtension
    ) {
        return fileExtension
                == DocumentFileExtension.DOCX;
    }

    @Override
    public ParsedDocument parse(
            SourceDocument sourceDocument,
            Path documentPath
    ) {
        try (
                InputStream inputStream =
                        Files.newInputStream(
                                documentPath
                        );

                XWPFDocument document =
                        new XWPFDocument(
                                inputStream
                        )
        ) {
            List<DocumentBlock> blocks =
                    new ArrayList<>();

            TreeMap<Integer, String> headings =
                    new TreeMap<>();

            for (
                    IBodyElement element :
                    document.getBodyElements()
            ) {
                if (element.getElementType()
                        == BodyElementType.PARAGRAPH) {

                    parseParagraph(
                            (XWPFParagraph) element,
                            document,
                            headings,
                            blocks
                    );

                    continue;
                }

                if (element.getElementType()
                        == BodyElementType.TABLE) {

                    parseTable(
                            (XWPFTable) element,
                            headings,
                            blocks
                    );
                }
            }

            if (blocks.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.DOCUMENT_TEXT_EMPTY
                );
            }

            return new ParsedDocument(
                    blocks
            );

        } catch (BusinessException exception) {
            throw exception;

        } catch (Exception exception) {

            log.error(
                    "DOCX 문서 파싱 실패. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }

    private void parseParagraph(
            XWPFParagraph paragraph,
            XWPFDocument document,
            TreeMap<Integer, String> headings,
            List<DocumentBlock> blocks
    ) {
        String text =
                normalize(
                        paragraph.getText()
                );

        if (text == null) {
            return;
        }

        Integer headingLevel =
                resolveHeadingLevel(
                        paragraph,
                        document
                );

        if (headingLevel != null) {

            headings.tailMap(
                    headingLevel,
                    true
            ).clear();

            headings.put(
                    headingLevel,
                    text
            );

            blocks.add(
                    new DocumentBlock(
                            null,
                            headingLevel,
                            buildHeadingPath(
                                    headings
                            ),
                            text,
                            DocumentChunkContentType.TEXT,
                            null,
                            text,
                            text,
                            DocumentExtractionMethod.NATIVE
                    )
            );

            return;
        }

        String content = applyListMarker(paragraph, text);

        blocks.add(
                new DocumentBlock(
                        null,
                        null,
                        buildHeadingPath(
                                headings
                        ),
                        currentSectionTitle(
                                headings
                        ),
                        DocumentChunkContentType.TEXT,
                        null,
                        content,
                        text,
                        DocumentExtractionMethod.NATIVE
                )
        );
    }

    private void parseTable(
            XWPFTable table,
            TreeMap<Integer, String> headings,
            List<DocumentBlock> blocks
    ) {
        String tableText =
                extractTableText(
                        table
                );

        if (tableText == null) {
            return;
        }

        blocks.add(
                new DocumentBlock(
                        null,
                        null,
                        buildHeadingPath(
                                headings
                        ),
                        currentSectionTitle(
                                headings
                        ),
                        DocumentChunkContentType.TABLE,
                        null,
                        tableText,
                        tableText,
                        DocumentExtractionMethod.NATIVE
                )
        );
    }

    private Integer resolveHeadingLevel(
            XWPFParagraph paragraph,
            XWPFDocument document
    ) {
        String style = paragraph.getStyle();
        String styleName = resolveStyleName(document, style);

        if ((style == null || style.isBlank())
                && (styleName == null || styleName.isBlank())) {
            return null;
        }

        Matcher matcher = HEADING_STYLE_PATTERN.matcher(
                ((style == null ? "" : style) + " " + (styleName == null ? "" : styleName)).trim()
        );

        if (!matcher.matches()) {
            if (TITLE_STYLE_PATTERN.matcher(style == null ? "" : style).matches()
                    || TITLE_STYLE_PATTERN.matcher(styleName == null ? "" : styleName).matches()) {
                return 1;
            }
            return null;
        }

        try {
            return Integer.parseInt(
                    matcher.group(1)
            );

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String resolveStyleName(XWPFDocument document, String styleId) {
        if (document == null || styleId == null || styleId.isBlank() || document.getStyles() == null) {
            return null;
        }

        XWPFStyle style = document.getStyles().getStyle(styleId);
        return style == null ? null : style.getName();
    }

    private String applyListMarker(XWPFParagraph paragraph, String text) {
        if (paragraph.getNumID() == null) return text;

        int level = paragraph.getNumIlvl() == null
                ? 0
                : paragraph.getNumIlvl().intValue();
        return "  ".repeat(Math.max(0, Math.min(level, 8))) + "• " + text;
    }

    private String buildHeadingPath(
            TreeMap<Integer, String> headings
    ) {
        if (headings.isEmpty()) {
            return null;
        }

        return String.join(
                " > ",
                headings.values()
        );
    }

    private String currentSectionTitle(
            TreeMap<Integer, String> headings
    ) {
        if (headings.isEmpty()) {
            return null;
        }

        Map.Entry<Integer, String> entry =
                headings.lastEntry();

        return entry == null
                ? null
                : entry.getValue();
    }

    private String extractTableText(
            XWPFTable table
    ) {
        List<String> rows =
                new ArrayList<>();

        for (XWPFTableRow row :
                table.getRows()) {

            List<String> cells =
                    new ArrayList<>();

            for (XWPFTableCell cell :
                    row.getTableCells()) {

                String text =
                        normalize(
                                cell.getText()
                        );

                cells.add(
                        text == null
                                ? ""
                                : text.replace(
                                "\n",
                                " "
                        )
                );
            }

            String rowText =
                    String.join(
                            " | ",
                            cells
                    ).trim();

            if (!rowText.isBlank()) {
                rows.add(rowText);
            }
        }

        return rows.isEmpty()
                ? null
                : String.join(
                "\n",
                rows
        );
    }

    private String normalize(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            return null;
        }

        String normalized =
                text.replace("\r\n", "\n")
                        .replace('\r', '\n')
                        .replace('\u00A0', ' ')
                        .replaceAll("[\\t ]+", " ")
                        .replaceAll("\\n{3,}", "\n\n")
                        .trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}
