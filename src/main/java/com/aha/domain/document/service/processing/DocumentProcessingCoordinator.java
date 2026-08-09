package com.aha.domain.document.service.processing;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.enums.DocumentProcessingStep;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.service.processing.embedding.DocumentEmbeddingService;
import com.aha.domain.document.service.processing.extraction.DocumentExtractionPipelineService;
import com.aha.domain.document.service.processing.extraction.model.ExtractedDocumentContext;
import com.aha.domain.document.service.processing.mapping.DocumentScopeMappingService;
import com.aha.domain.document.service.processing.model.DocumentProcessingContext;
import com.aha.domain.learningnote.service.generation.LearningNoteContentGenerationService;
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

    private final DocumentExtractionPipelineService extractionPipelineService;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentEmbeddingService embeddingService;
    private final DocumentScopeMappingService scopeMappingService;
    private final LearningNoteContentGenerationService contentGenerationService;
    private final DocumentProcessingStatusService processingStatusService;

    public void process(DocumentProcessingContext context) {
        validateContext(context);

        log.info(
                "문서 처리 파이프라인 시작. processingId={}, learningNoteId={}, sourceDocumentId={}",
                context.processingId(),
                context.learningNoteId(),
                context.sourceDocumentId()
        );

        ExtractedDocumentContext extractedDocument = extractionPipelineService.extractDocument(context.sourceDocumentId());

        processingStatusService.changeStep(
                context.processingId(),
                DocumentProcessingStep.CHUNKING
        );

        extractionPipelineService.createChunks(extractedDocument);

        List<DocumentChunk> chunks = documentChunkRepository
                .findAllBySourceDocument_IdOrderByChunkOrderAsc(
                        extractedDocument.sourceDocument().getId()
                );

        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        processingStatusService.changeStep(
                context.processingId(),
                DocumentProcessingStep.EMBEDDING
        );
        embeddingService.ensureChunkEmbeddings(chunks);

        processingStatusService.changeStep(
                context.processingId(),
                DocumentProcessingStep.SCOPE_MAPPING
        );
        scopeMappingService.mapDocuments(context.learningNoteId());

        processingStatusService.changeStep(
                context.processingId(),
                DocumentProcessingStep.CONTENT_GENERATING
        );
        contentGenerationService.generate(context.learningNoteId());

        log.info(
                "문서 처리 파이프라인 완료. processingId={}, learningNoteId={}, chunkCount={}",
                context.processingId(),
                context.learningNoteId(),
                chunks.size()
        );
    }

    private void validateContext(DocumentProcessingContext context) {
        if (context == null
                || context.processingId() == null
                || context.processingId() <= 0
                || context.learningNoteId() == null
                || context.learningNoteId() <= 0
                || context.sourceDocumentId() == null
                || context.sourceDocumentId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
