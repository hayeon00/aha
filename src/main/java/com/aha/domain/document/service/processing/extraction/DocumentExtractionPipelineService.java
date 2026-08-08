package com.aha.domain.document.service.processing.extraction;

import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.repository.SourceDocumentRepository;
import com.aha.domain.document.service.processing.chunk.DocumentChunkService;
import com.aha.domain.document.service.processing.extraction.model.ExtractedDocument;
import com.aha.domain.document.service.processing.extraction.model.ExtractedDocumentContext;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExtractionPipelineService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentTextExtractionService documentTextExtractionService;
    private final DocumentChunkService documentChunkService;

    public ExtractedDocumentContext extractDocument(Long sourceDocumentId) {
        SourceDocument sourceDocument = getSourceDocument(sourceDocumentId);
        ExtractedDocument extractedDocument = extract(sourceDocument);

        return new ExtractedDocumentContext(
                sourceDocument,
                extractedDocument
        );
    }

    public int createChunks(ExtractedDocumentContext context) {
        validateContext(context);
        return createDocumentChunks(context);
    }

    private ExtractedDocument extract(SourceDocument sourceDocument) {
        log.info(
                "문서 텍스트 추출 시작. sourceDocumentId={}, fileName={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName()
        );

        ExtractedDocument extractedDocument =
                documentTextExtractionService.extract(sourceDocument);

        log.info(
                "문서 텍스트 추출 완료. sourceDocumentId={}, blockCount={}",
                sourceDocument.getId(),
                extractedDocument.blocks().size()
        );

        return extractedDocument;
    }

    private int createDocumentChunks(
            ExtractedDocumentContext context
    ) {
        SourceDocument sourceDocument =
                context.sourceDocument();

        ExtractedDocument extractedDocument =
                context.extractedDocument();

        log.info(
                "문서 청크 생성 시작. sourceDocumentId={}, fileName={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName()
        );

        int chunkCount = documentChunkService.createChunks(
                sourceDocument,
                extractedDocument.blocks()
        );

        log.info(
                "문서 청크 생성 완료. sourceDocumentId={}, chunkCount={}",
                sourceDocument.getId(),
                chunkCount
        );

        return chunkCount;
    }


    private SourceDocument getSourceDocument(Long sourceDocumentId) {
        validateSourceDocumentId(sourceDocumentId);

        return sourceDocumentRepository.findById(sourceDocumentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SOURCE_DOCUMENT_NOT_FOUND
                ));
    }

    private void validateSourceDocumentId(
            Long sourceDocumentId
    ) {
        if (sourceDocumentId == null
                || sourceDocumentId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private void validateContext(ExtractedDocumentContext context) {
        if (context == null
                || context.sourceDocument() == null
                || context.extractedDocument() == null) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }
}
