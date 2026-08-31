package com.aha.domain.document.service.processing.parsing;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentFileExtension;
import com.aha.domain.document.repository.SourceDocumentRepository;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.domain.document.service.processing.parsing.parser.DocumentParser;
import com.aha.domain.document.service.storage.DocumentFileStorageService;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParsingService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentFileStorageService documentFileStorageService;
    private final List<DocumentParser> documentParsers;

    public ParsedDocument parse(
            Long sourceDocumentId
    ) {
        SourceDocument sourceDocument =
                getSourceDocument(
                        sourceDocumentId
                );

        Path documentPath =
                documentFileStorageService
                        .resolveForRead(
                                sourceDocument.getStorageKey()
                        );

        DocumentParser parser =
                findParser(
                        sourceDocument.getFileExtension()
                );

        log.info(
                "문서 파싱 시작. sourceDocumentId={}, fileName={}, extension={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName(),
                sourceDocument.getFileExtension()
        );

        ParsedDocument parsedDocument =
                parser.parse(
                        sourceDocument,
                        documentPath
                );

        if (parsedDocument == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }

        log.info(
                "문서 파싱 완료. sourceDocumentId={}, blockCount={}, textLength={}",
                sourceDocument.getId(),
                parsedDocument.blocks().size(),
                parsedDocument.totalTextLength()
        );

        return parsedDocument;
    }

    private DocumentParser findParser(
            DocumentFileExtension fileExtension
    ) {
        if (fileExtension == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }

        return documentParsers.stream()
                .filter(parser ->
                        parser.supports(
                                fileExtension
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
                        )
                );
    }

    private SourceDocument getSourceDocument(
            Long sourceDocumentId
    ) {
        if (sourceDocumentId == null
                || sourceDocumentId <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }

        return sourceDocumentRepository
                .findById(
                        sourceDocumentId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SOURCE_DOCUMENT_NOT_FOUND
                        )
                );
    }
}