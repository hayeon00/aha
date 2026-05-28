package com.aha.domain.ailearn.document.service;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfTextExtractionService {

    public List<ExtractedPageText> extractByPage(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new BusinessException(ErrorCode.STORED_FILE_NOT_FOUND);
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_DOCUMENT_TYPE);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            List<ExtractedPageText> extractedPageTexts = new ArrayList<>();

            int totalPages = document.getNumberOfPages();

            for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
                stripper.setStartPage(pageNo);
                stripper.setEndPage(pageNo);

                String text = stripper.getText(document);

                if (text != null && !text.trim().isEmpty()) {
                    extractedPageTexts.add(
                            new ExtractedPageText(pageNo, text.trim())
                    );
                }
            }

            return extractedPageTexts;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_EXTRACTION_FAILED);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ExtractedPageText {
        private int pageNo;
        private String text;
    }
}