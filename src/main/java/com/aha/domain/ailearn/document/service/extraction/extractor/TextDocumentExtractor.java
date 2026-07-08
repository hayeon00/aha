package com.aha.domain.ailearn.document.service.extraction.extractor;

import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import com.aha.domain.ailearn.document.service.extraction.model.DocumentBlock;
import com.aha.domain.ailearn.document.service.extraction.model.ExtractedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : TextDocumentExtractor
 * @since : 2026. 7. 8. 수요일
 */

@Slf4j
@Component
public class TextDocumentExtractor implements DocumentExtractor {

    @Override
    public boolean supports(String extension) {
        return "txt".equalsIgnoreCase(extension);
    }

    @Override
    public ExtractedDocument extract(SourceDocument sourceDocument, Path documentPath) {
        try {
            String extractedText = Files.readString(
                    documentPath,
                    StandardCharsets.UTF_8
            );

            validateExtractedText(sourceDocument, extractedText);

            DocumentBlock block = new DocumentBlock(
                    null,
                    null,
                    null,
                    DocumentChunkContentType.TEXT,
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
                    "TXT 문서 텍스트 추출 실패. sourceDocumentId={}, fileName={}",
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
                    "TXT 문서에서 텍스트를 추출하지 못했습니다. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName()
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }
    }
}