package com.aha.domain.notestudio.document.service.extraction.extractor;

import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.enums.DocumentChunkContentType;
import com.aha.domain.notestudio.document.service.extraction.model.DocumentBlock;
import com.aha.domain.notestudio.document.service.extraction.model.ExtractedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : PdfDocumentExtractor
 * @since : 2026. 7. 8. 수요일
 */

@Slf4j
@Component
public class PdfDocumentExtractor implements DocumentExtractor {

    private final Tika tika = new Tika();
    private static final int MAX_EXTRACTED_TEXT_LENGTH = 5_000_000;

    @Override
    public boolean supports(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }

    @Override
    public ExtractedDocument extract(SourceDocument sourceDocument, Path documentPath) {
        try (InputStream inputStream = Files.newInputStream(documentPath)) {
            Metadata metadata = new Metadata();

            metadata.set(
                    TikaCoreProperties.RESOURCE_NAME_KEY,
                    sourceDocument.getOriginalFileName()
            );

            String extractedText = tika.parseToString(
                    inputStream,
                    metadata,
                    MAX_EXTRACTED_TEXT_LENGTH
            );

            validateExtractedText(sourceDocument, extractedText);

            DocumentBlock block = new DocumentBlock(
                    null,
                    null,
                    null,
                    DocumentChunkContentType.TEXT,
                    null,
                    extractedText,
                    extractedText
            );

            return ExtractedDocument.of(
                    extractedText,
                    List.of(block)
            );

        } catch (BusinessException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "PDF 문서 텍스트 추출 실패. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }
    }

    private void validateExtractedText(SourceDocument sourceDocument, String extractedText) {
        if (extractedText == null || extractedText.isBlank()) {
            log.warn(
                    "PDF 문서에서 텍스트를 추출하지 못했습니다. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName()
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }
    }
}