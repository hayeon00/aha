package com.aha.domain.document.service.processing.parsing.ocr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentOcrServiceTest {

    @Test
    void installedTesseractExtractsEnglishText() {
        List<Path> knownDataPaths = List.of(
                Path.of("/opt/local/share/tessdata"),
                Path.of("/opt/homebrew/share/tessdata"),
                Path.of("/usr/local/share/tessdata"),
                Path.of("/usr/share/tesseract-ocr/5/tessdata")
        );

        Assumptions.assumeTrue(
                knownDataPaths.stream()
                        .anyMatch(path -> Files.isRegularFile(path.resolve("eng.traineddata"))),
                "Tesseract eng 학습 데이터가 설치된 환경에서만 OCR 통합 테스트를 실행합니다."
        );

        DocumentOcrService service = new DocumentOcrService(
                "",
                "",
                "eng"
        );

        BufferedImage image = new BufferedImage(
                1000,
                250,
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
            graphics.drawString("DATABASE TEST", 90, 145);
        } finally {
            graphics.dispose();
        }

        String extracted = service.extractText(image);

        assertThat(extracted)
                .isNotBlank()
                .containsIgnoringCase("DATABASE")
                .containsIgnoringCase("TEST");
    }
}
