package com.aha.domain.document.service.processing.parsing.parser;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.service.processing.parsing.model.DocumentExtractionMethod;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocxDocumentParserTest {

    @TempDir
    Path tempDirectory;

    @Test
    void preservesHeadingPathParagraphsAndTables() throws Exception {
        Path docx = tempDirectory.resolve("sample.docx");
        createDocx(docx);
        DocxDocumentParser parser = new DocxDocumentParser();

        var parsed = parser.parse(sourceDocument(), docx);

        assertThat(parsed.blocks()).hasSize(4);
        assertThat(parsed.blocks().get(0).headingLevel()).isEqualTo(1);
        assertThat(parsed.blocks().get(1).headingLevel()).isEqualTo(2);
        assertThat(parsed.blocks().get(2).headingPath())
                .isEqualTo("Database > Entity");
        assertThat(parsed.blocks().get(3).contentType())
                .isEqualTo(DocumentChunkContentType.TABLE);
        assertThat(parsed.blocks().get(3).text()).contains("Term | Meaning");
        assertThat(parsed.blocks())
                .allMatch(block -> block.extractionMethod() == DocumentExtractionMethod.NATIVE);
    }

    private SourceDocument sourceDocument() {
        SourceDocument sourceDocument = mock(SourceDocument.class);
        when(sourceDocument.getId()).thenReturn(1L);
        when(sourceDocument.getOriginalFileName()).thenReturn("sample.docx");
        return sourceDocument;
    }

    private void createDocx(Path path) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            var heading1 = document.createParagraph();
            heading1.setStyle("Heading1");
            heading1.createRun().setText("Database");

            var heading2 = document.createParagraph();
            heading2.setStyle("Heading2");
            heading2.createRun().setText("Entity");

            document.createParagraph().createRun()
                    .setText("An entity represents a business object with an identity.");

            var table = document.createTable(1, 2);
            table.getRow(0).getCell(0).setText("Term");
            table.getRow(0).getCell(1).setText("Meaning");

            try (OutputStream output = Files.newOutputStream(path)) {
                document.write(output);
            }
        }
    }
}
