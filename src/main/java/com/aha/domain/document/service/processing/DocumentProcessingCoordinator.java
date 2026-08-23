package com.aha.domain.document.service.processing;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.enums.DocumentProcessingStep;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.service.processing.chunking.DocumentChunkService;
import com.aha.domain.document.service.processing.embedding.DocumentEmbeddingService;
import com.aha.domain.document.service.processing.mapping.DocumentScopeMappingService;
import com.aha.domain.document.service.processing.model.DocumentProcessingContext;
import com.aha.domain.document.service.processing.parsing.DocumentParsingService;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.domain.document.service.processing.parsing.quality.DocumentQualityValidator;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingCoordinator {

    private final DocumentParsingService documentParsingService;
    private final DocumentQualityValidator documentQualityValidator;
    private final DocumentChunkService documentChunkService;

    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentEmbeddingService embeddingService;
    private final DocumentScopeMappingService scopeMappingService;
    //private final LearningNoteContentGenerationService contentGenerationService;

    private final DocumentProcessingStatusService processingStatusService;

    public void process(
            DocumentProcessingContext context
    ) {
        validateContext(context);

        log.info(
                "문서 처리 파이프라인 시작. processingId={}, learningNoteId={}, sourceDocumentId={}",
                context.processingId(),
                context.learningNoteId(),
                context.sourceDocumentId()
        );

        ParsedDocument parsedDocument =
                documentParsingService.parse(
                        context.sourceDocumentId()
                );


        logParsedDocument(
                context,
                parsedDocument
        );


        changeStep(
                context,
                DocumentProcessingStep.QUALITY_CHECK
        );

        documentQualityValidator.validate(
                parsedDocument
        );



        // =====================================================================
        changeStep(
                context,
                DocumentProcessingStep.CHUNKING
        );

        int chunkCount =
                documentChunkService.createChunks(
                        context.sourceDocumentId(),
                        parsedDocument
                );

        List<DocumentChunk> chunks =
                getCreatedChunks(
                        context.sourceDocumentId()
                );

        log.info(
                "파싱/청킹 테스트 완료. processingId={}, sourceDocumentId={}, parsedBlockCount={}, chunkCount={}",
                context.processingId(),
                context.sourceDocumentId(),
                parsedDocument.blocks().size(),
                chunkCount
        );


        logChunkSummary(
                context,
                chunks
        );

//
//        changeStep(
//                context,
//                DocumentProcessingStep.EMBEDDING
//        );
//
//        embeddingService.ensureChunkEmbeddings(
//                chunks
//        );


        changeStep(
                context,
                DocumentProcessingStep.SCOPE_MAPPING
        );

        scopeMappingService.mapDocuments(
                context.learningNoteId()
        );
//
//
//        changeStep(
//                context,
//                DocumentProcessingStep.CONTENT_GENERATING
//        );
//
//        contentGenerationService.generate(
//                context.learningNoteId()
//        );
//
//
//        changeStep(
//                context,
//                DocumentProcessingStep.FINALIZING
//        );
//
//        log.info(
//                "문서 처리 파이프라인 실행 완료. processingId={}, learningNoteId={}, chunkCount={}",
//                context.processingId(),
//                context.learningNoteId(),
//                chunks.size()
//        );
    }

    private void logParsedDocument(
            DocumentProcessingContext context,
            ParsedDocument parsedDocument
    ) {
        log.info(
                "ParsedDocument 확인. processingId={}, blockCount={}, totalTextLength={}",
                context.processingId(),
                parsedDocument.blocks().size(),
                parsedDocument.totalTextLength()
        );

        for (int i = 0;
             i < parsedDocument.blocks().size();
             i++) {

            var block =
                    parsedDocument.blocks().get(i);

            log.info(
                    "ParsedBlock[{}] pageNo={}, headingLevel={}, headingPath={}, sectionTitle={}, contentType={}, extractionMethod={}, codeLanguage={}, textLength={}, preview={}",
                    i + 1,
                    block.pageNo(),
                    block.headingLevel(),
                    block.headingPath(),
                    block.sectionTitle(),
                    block.contentType(),
                    block.resolvedExtractionMethod(),
                    block.codeLanguage(),
                    block.text() == null
                            ? 0
                            : block.text().length(),
                    preview(block.text())
            );
        }
    }

    private void logChunkSummary(
            DocumentProcessingContext context,
            List<DocumentChunk> chunks
    ) {
        for (DocumentChunk chunk : chunks) {
            log.info(
                    "DocumentChunk 확인. processingId={}, chunkId={}, chunkOrder={}, pageStart={}, pageEnd={}, headingPath={}, sectionTitle={}, contentType={}, textLength={}, preview={}",
                    context.processingId(),
                    chunk.getId(),
                    chunk.getChunkOrder(),
                    chunk.getPageStart(),
                    chunk.getPageEnd(),
                    chunk.getHeadingPath(),
                    chunk.getSectionTitle(),
                    chunk.getContentType(),
                    chunk.getContentText() == null
                            ? 0
                            : chunk.getContentText().length(),
                    preview(chunk.getContentText())
            );
        }
    }

    private List<DocumentChunk> getCreatedChunks(
            Long sourceDocumentId
    ) {
        List<DocumentChunk> chunks =
                documentChunkRepository
                        .findAllBySourceDocument_IdOrderByChunkOrderAsc(
                                sourceDocumentId
                        );

        if (chunks.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_CHUNK_NOT_FOUND
            );
        }

        return chunks;
    }

    private void changeStep(
            DocumentProcessingContext context,
            DocumentProcessingStep nextStep
    ) {
        processingStatusService.changeStep(
                context.processingId(),
                nextStep
        );
    }

    private void validateContext(
            DocumentProcessingContext context
    ) {
        if (context == null
                || context.processingId() == null
                || context.processingId() <= 0
                || context.learningNoteId() == null
                || context.learningNoteId() <= 0
                || context.sourceDocumentId() == null
                || context.sourceDocumentId() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }


    private String preview(
            String text
    ) {
        if (text == null
                || text.isBlank()) {
            return "";
        }

        String normalized =
                text.replace(
                                "\n",
                                " "
                        )
                        .trim();

        return normalized.substring(
                0,
                Math.min(
                        normalized.length(),
                        200
                )
        );
    }
}
