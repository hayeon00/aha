package com.aha.domain.document.service.processing.parsing.parser;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.service.processing.parsing.model.DocumentBlock;
import com.aha.domain.document.service.processing.parsing.model.DocumentExtractionMethod;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.domain.document.service.processing.parsing.ocr.DocumentOcrService;
import com.aha.domain.document.service.processing.parsing.quality.DocumentTextQualityAnalyzer;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfDocumentParser
        implements DocumentParser {

    private static final int MIN_NATIVE_TEXT_LENGTH = 30;
    private static final float OCR_DPI = 300F;
    private static final int MAX_HEADING_LENGTH = 120;
    private static final int MAX_PLAIN_HEADING_LENGTH = 40;
    private static final int MAX_PLAIN_HEADING_WORD_COUNT = 8;

    private static final Pattern NUMBERED_HEADING = Pattern.compile(
            "^(?:제\\s*\\d+\\s*(?:장|절|과목)|\\d+(?:\\.\\d+)*(?:[.)])?)\\s+\\S.+$"
    );
    private static final Pattern MARKDOWN_HEADING = Pattern.compile("^#{1,6}\\s+\\S.+$");

    private final DocumentOcrService documentOcrService;
    private final DocumentTextQualityAnalyzer qualityAnalyzer;

    @Override
    public boolean supports(
            DocumentFileExtension fileExtension
    ) {
        return fileExtension
                == DocumentFileExtension.PDF;
    }

    @Override
    public ParsedDocument parse(
            SourceDocument sourceDocument,
            Path documentPath
    ) {
        try (
                PDDocument document =
                        Loader.loadPDF(
                                documentPath.toFile()
                        )
        ) {
            PDFTextStripper stripper =
                    new PDFTextStripper();
            stripper.setSortByPosition(true);

            PDFRenderer renderer =
                    new PDFRenderer(
                            document
                    );

            List<DocumentBlock> blocks =
                    new ArrayList<>();

            /*
             * PDF의 Section이 여러 페이지에 걸쳐 이어질 수 있으므로
             * 직전 페이지의 heading 상태를 다음 페이지로 전달한다.
             */
            HeadingContext headingContext =
                    HeadingContext.empty();

            for (
                    int pageNo = 1;
                    pageNo <= document.getNumberOfPages();
                    pageNo++
            ) {
                String nativeText =
                        extractNativeText(
                                document,
                                stripper,
                                pageNo
                        );

                PageExtraction extraction = selectBestExtraction(
                        renderer,
                        pageNo,
                        nativeText
                );
                String pageText = extraction.text();

                if (pageText == null
                        || pageText.isBlank()) {

                    log.warn(
                            "PDF 페이지에서 텍스트를 추출하지 못했습니다. "
                                    + "sourceDocumentId={}, pageNo={}",
                            sourceDocument.getId(),
                            pageNo
                    );

                    continue;
                }

                PageParseResult pageParseResult =
                        createPageBlocks(
                                pageNo,
                                pageText,
                                extraction.method(),
                                headingContext
                        );

                blocks.addAll(
                        pageParseResult.blocks()
                );

                headingContext =
                        pageParseResult.headingContext();

                log.info(
                        "PDF 페이지 파싱 완료. "
                                + "sourceDocumentId={}, pageNo={}, "
                                + "textLength={}, ocrUsed={}",
                        sourceDocument.getId(),
                        pageNo,
                        pageText.length(),
                        extraction.method() == DocumentExtractionMethod.OCR
                );
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
                    "PDF 문서 파싱 실패. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    exception
            );

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }

    private String extractNativeText(
            PDDocument document,
            PDFTextStripper stripper,
            int pageNo
    ) throws Exception {

        stripper.setStartPage(
                pageNo
        );

        stripper.setEndPage(
                pageNo
        );

        return normalize(
                stripper.getText(
                        document
                )
        );
    }

    private String extractTextWithOcr(
            PDFRenderer renderer,
            int pageNo
    ) throws Exception {

        BufferedImage image =
                renderer.renderImageWithDPI(
                        pageNo - 1,
                        OCR_DPI,
                        ImageType.RGB
                );

        return documentOcrService
                .extractText(
                        image
                );
    }

    private PageExtraction selectBestExtraction(
            PDFRenderer renderer,
            int pageNo,
            String nativeText
    ) {
        if (!shouldTryOcr(nativeText)) {
            return new PageExtraction(nativeText, DocumentExtractionMethod.NATIVE);
        }

        try {
            String ocrText = normalize(extractTextWithOcr(renderer, pageNo));
            double nativeScore = qualityAnalyzer.analyze(nativeText).score();
            double ocrScore = qualityAnalyzer.analyze(ocrText).score();

            if (ocrText != null
                    && qualityAnalyzer.isAcceptable(ocrText)
                    && (nativeText == null || ocrScore > nativeScore + 0.05)) {
                return new PageExtraction(ocrText, DocumentExtractionMethod.OCR);
            }
        } catch (Exception exception) {
            if (nativeText == null || nativeText.isBlank()) {
                throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
            }
            log.warn(
                    "OCR 처리에 실패해 PDF 네이티브 텍스트를 사용합니다. pageNo={}",
                    pageNo,
                    exception
            );
        }

        return new PageExtraction(nativeText, DocumentExtractionMethod.NATIVE);
    }

    private boolean shouldTryOcr(String nativeText) {
        if (nativeText == null || nativeText.isBlank()) return true;

        int compactLength = nativeText.replaceAll("\\s+", "").length();
        return compactLength < MIN_NATIVE_TEXT_LENGTH
                || !qualityAnalyzer.isAcceptable(nativeText);
    }

    private PageParseResult createPageBlocks(
            int pageNo,
            String pageText,
            DocumentExtractionMethod extractionMethod,
            HeadingContext inheritedHeadingContext
    ) {

        List<DocumentBlock> blocks =
                new ArrayList<>();

        StringBuilder body =
                new StringBuilder();

        HeadingContext safeHeadingContext =
                inheritedHeadingContext == null
                        ? HeadingContext.empty()
                        : inheritedHeadingContext;

        String currentHeading =
                safeHeadingContext.heading();

        Integer currentHeadingLevel =
                safeHeadingContext.level();

        for (String rawLine : pageText.split("\\n")) {

            String line = normalize(rawLine);

            if (line == null) {
                continue;
            }

            /*
             * 요약노트형 정의 bullet 처리.
             *
             * 예:
             * • 엔터티: 업무에서 관리할 필요가 있는 데이터 집합
             * - JOIN: 둘 이상의 테이블을 연결해 조회
             *
             * bullet + "짧은 용어 : 설명" 형태일 때만
             * 독립적인 Section으로 분리한다.
             */
            DefinitionBullet definitionBullet =
                    parseDefinitionBullet(line);

            if (definitionBullet != null) {

                // bullet 이전에 누적된 일반 본문을 먼저 저장
                flushSectionBlock(
                        blocks,
                        body,
                        pageNo,
                        currentHeading,
                        currentHeadingLevel,
                        extractionMethod
                );

                // bullet 정의 자체를 독립 Block으로 저장
                flushDefinitionBlock(
                        blocks,
                        pageNo,
                        definitionBullet,
                        extractionMethod
                );

                continue;
            }

            // 새로운 제목 발견
            if (isHeadingCandidate(line)) {

                // 이전 제목에 속한 본문을 먼저 하나의 Block으로 저장
                flushSectionBlock(
                        blocks,
                        body,
                        pageNo,
                        currentHeading,
                        currentHeadingLevel,
                        extractionMethod
                );

                // 새로운 Section 시작
                currentHeading = stripMarkdownHeading(line);
                currentHeadingLevel = resolveHeadingLevel(line);

                continue;
            }

            // 일반 본문
            if (!body.isEmpty()) {
                body.append('\n');
            }

            body.append(line);
        }

        // 마지막 Section 저장
        flushSectionBlock(
                blocks,
                body,
                pageNo,
                currentHeading,
                currentHeadingLevel,
                extractionMethod
        );

        // 제목 구조를 찾지 못한 일반 문서
        if (blocks.isEmpty()
                && pageText != null
                && !pageText.isBlank()) {

            blocks.add(
                    new DocumentBlock(
                            pageNo,
                            null,
                            null,
                            null,
                            DocumentChunkContentType.TEXT,
                            null,
                            pageText,
                            pageText,
                            extractionMethod
                    )
            );
        }

        return new PageParseResult(
                List.copyOf(blocks),
                new HeadingContext(
                        currentHeading,
                        currentHeadingLevel
                )
        );
    }

    private int resolveHeadingLevel(
            String heading
    ) {

        String trimmed =
                heading.trim();

        // Markdown
        // # 제목     -> 1
        // ## 제목    -> 2
        // ### 제목   -> 3
        if (trimmed.startsWith("#")) {

            int level = 0;

            while (level < trimmed.length()
                    && trimmed.charAt(level) == '#') {

                level++;
            }

            return Math.min(
                    level,
                    6
            );
        }

        /*
         * 번호 기반
         *
         * 1. 제목       -> 1
         * 1.1 제목     -> 2
         * 1.1.1 제목   -> 3
         */

        String firstToken =
                trimmed.split("\\s+")[0];

        String numberPart =
                firstToken.replaceAll(
                        "[^0-9.]",
                        ""
                );

        if (!numberPart.isBlank()) {

            String normalized =
                    numberPart.replaceAll(
                            "\\.$",
                            ""
                    );

            if (!normalized.isBlank()) {

                return normalized
                        .split("\\.")
                        .length;
            }
        }

        return 1;
    }

    private void flushSectionBlock(
            List<DocumentBlock> blocks,
            StringBuilder body,
            int pageNo,
            String currentHeading,
            Integer headingLevel,
            DocumentExtractionMethod extractionMethod
    ) {

        String bodyText = normalize(
                body.toString()
        );

        body.setLength(0);

        if (bodyText == null) {
            return;
        }

        String contentText =
                currentHeading == null
                        ? bodyText
                        : currentHeading
                          + "\n\n"
                          + bodyText;

        blocks.add(
                new DocumentBlock(
                        pageNo,
                        headingLevel,
                        currentHeading,
                        currentHeading,
                        DocumentChunkContentType.TEXT,
                        null,

                        // 검색/매핑에 사용할 텍스트
                        contentText,

                        // 실제 본문
                        bodyText,

                        extractionMethod
                )
        );
    }


    /**
     * 요약노트형 정의 bullet을 파싱한다.
     *
     * 예:
     * • 엔터티: 업무에서 관리할 필요가 있는 데이터 집합
     * • SELECT: 필요한 컬럼과 행을 조회한다.
     *
     * 정규식 하나에 의존하지 않고
     * 1. bullet 문자 확인
     * 2. bullet 제거
     * 3. 첫 번째 ':' 또는 '：' 기준 분리
     *
     * 순서로 처리한다.
     */
    private DefinitionBullet parseDefinitionBullet(
            String line
    ) {
        if (line == null
                || line.isBlank()) {
            return null;
        }

        String trimmed =
                line.trim();

        if (!startsWithBullet(trimmed)) {
            return null;
        }

        String content =
                trimmed.substring(1)
                        .trim();

        int colonIndex =
                findDefinitionColon(content);

        if (colonIndex <= 0
                || colonIndex >= content.length() - 1) {
            return null;
        }

        String title =
                normalize(
                        content.substring(
                                0,
                                colonIndex
                        )
                );

        String body =
                normalize(
                        content.substring(
                                colonIndex + 1
                        )
                );

        if (title == null
                || body == null) {
            return null;
        }

        /*
         * 너무 긴 문자열이 ':' 앞에 있는 경우는
         * 실제 용어 정의라기보다 일반 문장일 가능성이 높다.
         */
        if (title.length() > 60) {
            return null;
        }

        return new DefinitionBullet(
                title,
                body
        );
    }

    private boolean startsWithBullet(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            return false;
        }

        char firstCharacter =
                text.charAt(0);

        return firstCharacter == '•'
                || firstCharacter == '●'
                || firstCharacter == '▪'
                || firstCharacter == '◦'
                || firstCharacter == '·'
                || firstCharacter == '-'
                || firstCharacter == '*'
                || firstCharacter == '+';
    }

    private int findDefinitionColon(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            return -1;
        }

        int normalColonIndex =
                text.indexOf(':');

        int fullWidthColonIndex =
                text.indexOf('：');

        if (normalColonIndex < 0) {
            return fullWidthColonIndex;
        }

        if (fullWidthColonIndex < 0) {
            return normalColonIndex;
        }

        return Math.min(
                normalColonIndex,
                fullWidthColonIndex
        );
    }

    /**
     * 한 줄 정의 bullet을 독립적인 DocumentBlock으로 저장한다.
     *
     * 예:
     *
     * sectionTitle = "엔터티"
     * contentText =
     *   "엔터티\n\n업무에서 관리할 필요가 있는 데이터 집합"
     * rawText =
     *   "업무에서 관리할 필요가 있는 데이터 집합"
     */
    private void flushDefinitionBlock(
            List<DocumentBlock> blocks,
            int pageNo,
            DefinitionBullet definitionBullet,
            DocumentExtractionMethod extractionMethod
    ) {
        if (definitionBullet == null) {
            return;
        }

        String title =
                normalize(
                        definitionBullet.title()
                );

        String body =
                normalize(
                        definitionBullet.body()
                );

        if (title == null
                || body == null) {
            return;
        }

        String contentText =
                title
                        + "\n\n"
                        + body;

        blocks.add(
                new DocumentBlock(
                        pageNo,

                        // bullet 정의는 numbered heading 계층이 아니므로 null
                        null,

                        // 계층형 headingPath가 아니므로 null
                        null,

                        title,
                        DocumentChunkContentType.TEXT,
                        null,

                        // 검색/매핑용 텍스트
                        contentText,

                        // 실제 본문
                        body,

                        extractionMethod
                )
        );
    }

    private boolean isHeadingCandidate(String line) {

        if (line == null || line.isBlank()) {
            return false;
        }

        if (line.length() > MAX_HEADING_LENGTH) {
            return false;
        }

        // 1. 기존의 명확한 제목 형식
        if (NUMBERED_HEADING.matcher(line).matches()
                || MARKDOWN_HEADING.matcher(line).matches()) {

            return true;
        }

        // 2. 번호가 없는 일반 텍스트형 제목
        return isPlainTextHeading(line);
    }

    private boolean isPlainTextHeading(String line) {

        if (line == null || line.isBlank()) {
            return false;
        }

        String trimmed = line.trim();

        // 너무 긴 문장은 제목으로 보지 않는다.
        if (trimmed.length() > MAX_PLAIN_HEADING_LENGTH) {
            return false;
        }

        // 단어가 너무 많으면 일반 문장일 가능성이 높다.
        int wordCount = trimmed.split("\\s+").length;

        if (wordCount > MAX_PLAIN_HEADING_WORD_COUNT) {
            return false;
        }

        // bullet은 별도 DefinitionBullet 처리 대상
        if (startsWithBullet(trimmed)) {
            return false;
        }

        // 일반 문장처럼 끝나는 경우 제목으로 보지 않는다.
        if (endsWithSentencePunctuation(trimmed)) {
            return false;
        }

        // 정의/설명문 형태는 제목으로 보지 않는다.
        if (trimmed.contains(":")
                || trimmed.contains("：")) {
            return false;
        }

        // 너무 짧은 단일 문자 등은 제외
        if (trimmed.length() < 2) {
            return false;
        }

        return true;
    }

    private boolean endsWithSentencePunctuation(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        char last =
                text.charAt(
                        text.length() - 1
                );

        return last == '.'
                || last == '!'
                || last == '?'
                || last == '。'
                || last == '！'
                || last == '？';
    }

    private String stripMarkdownHeading(String line) {
        return line.replaceFirst("^#{1,6}\\s+", "").trim();
    }

    private record DefinitionBullet(
            String title,
            String body
    ) {
    }

    /**
     * 페이지 파싱이 끝난 뒤 생성된 Block과
     * 다음 페이지에 전달할 마지막 heading 상태를 함께 반환한다.
     */
    private record PageParseResult(
            List<DocumentBlock> blocks,
            HeadingContext headingContext
    ) {
    }

    /**
     * 현재 Section의 heading 상태.
     * 다음 페이지가 제목 없이 본문부터 시작하면 이 context를 이어받는다.
     */
    private record HeadingContext(
            String heading,
            Integer level
    ) {

        private static HeadingContext empty() {
            return new HeadingContext(
                    null,
                    null
            );
        }
    }

    private record PageExtraction(
            String text,
            DocumentExtractionMethod method
    ) {
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