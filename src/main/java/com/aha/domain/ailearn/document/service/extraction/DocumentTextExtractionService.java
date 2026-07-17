package com.aha.domain.ailearn.document.service.extraction;

import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import com.aha.domain.ailearn.document.service.extraction.classifier.DocumentBlockClassifier;
import com.aha.domain.ailearn.document.service.extraction.extractor.DocumentExtractor;
import com.aha.domain.ailearn.document.service.extraction.model.DocumentBlock;
import com.aha.domain.ailearn.document.service.extraction.model.ExtractedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentTextExtractionService
 * @since : 2026. 7. 8. 수요일
 */

@Slf4j
@Service
public class DocumentTextExtractionService {

    private final List<DocumentExtractor> documentExtractors;
    private final Path baseUploadDirectory;
    private final DocumentBlockClassifier documentBlockClassifier;

    public DocumentTextExtractionService(
            List<DocumentExtractor> documentExtractors,
            DocumentBlockClassifier documentBlockClassifier,
            @Value("${file.document-upload-dir:uploads}") String documentUploadDir
    ) {
        this.documentExtractors = documentExtractors;
        this.documentBlockClassifier = documentBlockClassifier;
        this.baseUploadDirectory = Paths.get(documentUploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public ExtractedDocument extract(SourceDocument sourceDocument) {
        validateSourceDocument(sourceDocument);

        Path documentPath = resolveDocumentPath(sourceDocument);

        String extension = resolveExtension(sourceDocument, documentPath);

        DocumentExtractor documentExtractor = findExtractor(extension);

        ExtractedDocument extractedDocument = documentExtractor.extract(
                sourceDocument,
                documentPath
        );

        log.info(
                "문서 텍스트 추출 결과. sourceDocumentId={}, fileName={}, fullTextLength={}, blockCount={}, preview={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName(),
                extractedDocument.fullText() == null ? 0 : extractedDocument.fullText().length(),
                extractedDocument.blocks() == null ? 0 : extractedDocument.blocks().size(),
                preview(extractedDocument.fullText())
        );

        ExtractedDocument normalizedDocument =
                normalizeExtractedDocument(extractedDocument);

        log.info(
                "문서 정규화 결과. sourceDocumentId={}, fullTextLength={}, blockCount={}, blockTextLength={}",
                sourceDocument.getId(),
                normalizedDocument.fullText() == null ? 0 : normalizedDocument.fullText().length(),
                normalizedDocument.blocks() == null ? 0 : normalizedDocument.blocks().size(),
                calculateTextLength(normalizedDocument.blocks())
        );

        List<DocumentBlock> classifiedBlocks =
                documentBlockClassifier.classify(normalizedDocument.blocks());

        log.info(
                "문서 블록 분류 결과. sourceDocumentId={}, classifiedBlockCount={}, classifiedTextLength={}",
                sourceDocument.getId(),
                classifiedBlocks == null ? 0 : classifiedBlocks.size(),
                calculateTextLength(classifiedBlocks)
        );

        if (shouldFallbackToNormalizedDocument(normalizedDocument, classifiedBlocks)) {
            log.warn(
                    "문서 블록 분류 결과가 원문 대비 부족하여 원본 TEXT 블록으로 fallback 처리합니다. sourceDocumentId={}, originalLength={}, classifiedLength={}, classifiedBlockCount={}",
                    sourceDocument.getId(),
                    calculateTextLength(normalizedDocument.blocks()),
                    calculateTextLength(classifiedBlocks),
                    classifiedBlocks == null ? 0 : classifiedBlocks.size()
            );

            return normalizedDocument;
        }

        return ExtractedDocument.of(
                normalizedDocument.fullText(),
                classifiedBlocks
        );
    }

    private String preview(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalizedText = text.replace("\n", " ").trim();

        return normalizedText.substring(
                0,
                Math.min(300, normalizedText.length())
        );
    }





    private boolean shouldFallbackToNormalizedDocument(
            ExtractedDocument normalizedDocument,
            List<DocumentBlock> classifiedBlocks
    ) {
        if (classifiedBlocks == null || classifiedBlocks.isEmpty()) {
            return true;
        }

        int originalLength = calculateTextLength(normalizedDocument.blocks());
        int classifiedLength = calculateTextLength(classifiedBlocks);

        if (originalLength == 0) {
            return true;
        }

        double remainingRatio = (double) classifiedLength / originalLength;

        return remainingRatio < 0.5;
    }


    private int calculateTextLength(List<DocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return 0;
        }

        return blocks.stream()
                .filter(block -> block != null && block.text() != null)
                .mapToInt(block -> block.text().length())
                .sum();
    }

    private void validateSourceDocument(SourceDocument sourceDocument) {
        if (sourceDocument == null || sourceDocument.getId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Path resolveDocumentPath(SourceDocument sourceDocument) {
        String storageKey = sourceDocument.getStorageKey();

        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        Path documentPath = baseUploadDirectory.resolve(storageKey)
                .toAbsolutePath()
                .normalize();

        if (!documentPath.startsWith(baseUploadDirectory)) {
            log.warn(
                    "허용되지 않은 문서 경로입니다. sourceDocumentId={}, path={}",
                    sourceDocument.getId(),
                    documentPath
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        if (!Files.isRegularFile(documentPath)) {
            log.warn(
                    "저장된 문서 파일을 찾을 수 없습니다. sourceDocumentId={}, path={}",
                    sourceDocument.getId(),
                    documentPath
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        return documentPath;
    }

    private String resolveExtension(
            SourceDocument sourceDocument,
            Path documentPath
    ) {
        String originalFileName = sourceDocument.getOriginalFileName();

        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = documentPath.getFileName().toString();
        }

        int extensionIndex = originalFileName.lastIndexOf('.');

        if (extensionIndex <= 0 || extensionIndex == originalFileName.length() - 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        return originalFileName.substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private DocumentExtractor findExtractor(String extension) {
        return documentExtractors.stream()
                .filter(documentExtractor -> documentExtractor.supports(extension))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn(
                            "지원하지 않는 문서 추출 형식입니다. extension={}",
                            extension
                    );

                    return new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
                });
    }

    private ExtractedDocument normalizeExtractedDocument(ExtractedDocument extractedDocument) {
        if (extractedDocument == null) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        String normalizedFullText = normalizeText(extractedDocument.fullText());

        List<DocumentBlock> normalizedBlocks = extractedDocument.blocks()
                .stream()
                .filter(block -> block != null && !block.isBlank())
                .map(this::normalizeBlock)
                .filter(block -> !block.isBlank())
                .toList();

        if (normalizedBlocks.isEmpty()) {
            normalizedBlocks = List.of(
                    new DocumentBlock(
                            null,
                            null,
                            null,
                            DocumentChunkContentType.TEXT,
                            null,
                            normalizedFullText,
                            normalizedFullText
                    )
            );
        }

        return ExtractedDocument.of(
                normalizedFullText,
                normalizedBlocks
        );
    }

    private DocumentBlock normalizeBlock(DocumentBlock block) {
        String normalizedText = normalizeText(block.text());

        String normalizedRawText = block.rawText() == null || block.rawText().isBlank()
                ? normalizedText
                : normalizeText(block.rawText());

        DocumentChunkContentType contentType = block.contentType() == null
                ? DocumentChunkContentType.TEXT
                : block.contentType();

        return new DocumentBlock(
                block.pageNo(),
                normalizeNullableText(block.headingPath()),
                normalizeNullableText(block.sectionTitle()),
                contentType,
                block.resolvedCodeLanguage(),
                normalizedText,
                normalizedRawText
        );
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }

        String normalizedText = text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ');

        normalizedText = normalizedText.replaceAll("[\\t ]+", " ");

        normalizedText = normalizedText.replaceAll("\\n{3,}", "\n\n");

        normalizedText = normalizedText.trim();

        if (normalizedText.isBlank()) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }

        return normalizedText;
    }

    private String normalizeNullableText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ")
                .trim();
    }
}