package com.aha.domain.document.service.processing.parsing.parser;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.service.processing.parsing.model.DocumentExtractionMethod;
import com.aha.domain.document.service.processing.parsing.ocr.DocumentOcrService;
import com.aha.domain.document.service.processing.parsing.quality.DocumentTextQualityAnalyzer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfDocumentParserTest {

    @TempDir
    Path tempDirectory;

    @Test
    void nativePdfIsSplitIntoHeadingAndBodyBlocks() throws Exception {
        Path pdf = tempDirectory.resolve("native.pdf");
        createTextPdf(pdf, "1. Data Modeling", "Entity is a core object managed by a business system.");

        DocumentOcrService ocrService = mock(DocumentOcrService.class);
        PdfDocumentParser parser = new PdfDocumentParser(
                ocrService,
                new DocumentTextQualityAnalyzer()
        );

        var parsed = parser.parse(sourceDocument("native.pdf"), pdf);

        assertThat(parsed.blocks()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(parsed.blocks().get(0).isHeading()).isTrue();
        assertThat(parsed.blocks().get(0).sectionTitle()).isEqualTo("1. Data Modeling");
        assertThat(parsed.blocks())
                .allMatch(block -> block.pageNo() == 1)
                .allMatch(block -> block.extractionMethod() == DocumentExtractionMethod.NATIVE);
        assertThat(parsed.blocks())
                .anyMatch(block -> block.text().contains("core object"));
    }

    @Test
    void imageOnlyPdfUsesOcrAndPreservesItsOrigin() throws Exception {
        Path pdf = tempDirectory.resolve("scan.pdf");
        createBlankPdf(pdf);

        DocumentOcrService ocrService = mock(DocumentOcrService.class);
        when(ocrService.extractText(any())).thenReturn(
                "1. Database Fundamentals\nA database stores structured information for reliable retrieval."
        );
        PdfDocumentParser parser = new PdfDocumentParser(
                ocrService,
                new DocumentTextQualityAnalyzer()
        );

        var parsed = parser.parse(sourceDocument("scan.pdf"), pdf);

        assertThat(parsed.blocks()).isNotEmpty();
        assertThat(parsed.blocks())
                .allMatch(block -> block.extractionMethod() == DocumentExtractionMethod.OCR)
                .allMatch(block -> block.contentType() == DocumentChunkContentType.TEXT);
    }

    private SourceDocument sourceDocument(String fileName) {
        SourceDocument sourceDocument = mock(SourceDocument.class);
        when(sourceDocument.getId()).thenReturn(1L);
        when(sourceDocument.getOriginalFileName()).thenReturn(fileName);
        return sourceDocument;
    }

    private void createTextPdf(Path path, String heading, String body) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                stream.newLineAtOffset(70, 730);
                stream.showText(heading);
                stream.newLineAtOffset(0, -35);
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                stream.showText(body);
                stream.endText();
            }
            document.save(path.toFile());
        }
    }

    private void createBlankPdf(Path path) throws Exception {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(path.toFile());
        }
    }
}
