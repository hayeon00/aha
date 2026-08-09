package com.aha.domain.document.service.processing;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentProcessingStep;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.service.processing.embedding.DocumentEmbeddingService;
import com.aha.domain.document.service.processing.extraction.DocumentExtractionPipelineService;
import com.aha.domain.document.service.processing.extraction.model.ExtractedDocumentContext;
import com.aha.domain.document.service.processing.mapping.DocumentScopeMappingService;
import com.aha.domain.document.service.processing.model.DocumentProcessingContext;
import com.aha.domain.learningnote.service.generation.LearningNoteContentGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingCoordinatorTest {

    @Mock private DocumentExtractionPipelineService extractionService;
    @Mock private DocumentChunkRepository chunkRepository;
    @Mock private DocumentEmbeddingService embeddingService;
    @Mock private DocumentScopeMappingService mappingService;
    @Mock private LearningNoteContentGenerationService contentGenerationService;
    @Mock private DocumentProcessingStatusService statusService;

    private DocumentProcessingCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new DocumentProcessingCoordinator(
                extractionService,
                chunkRepository,
                embeddingService,
                mappingService,
                contentGenerationService,
                statusService
        );
    }

    @Test
    void 목차_매핑_후_CONTENT_GENERATING으로_전환하고_개념설명을_생성한다() {
        DocumentProcessingContext context = new DocumentProcessingContext(1L, 10L, 20L);
        SourceDocument sourceDocument = mock(SourceDocument.class);
        ExtractedDocumentContext extracted = mock(ExtractedDocumentContext.class);
        DocumentChunk chunk = mock(DocumentChunk.class);

        when(extractionService.extractDocument(20L)).thenReturn(extracted);
        when(extracted.sourceDocument()).thenReturn(sourceDocument);
        when(sourceDocument.getId()).thenReturn(20L);
        when(chunkRepository.findAllBySourceDocument_IdOrderByChunkOrderAsc(20L))
                .thenReturn(List.of(chunk));

        coordinator.process(context);

        InOrder order = inOrder(statusService, mappingService, contentGenerationService);
        order.verify(statusService).changeStep(1L, DocumentProcessingStep.SCOPE_MAPPING);
        order.verify(mappingService).mapDocuments(10L);
        order.verify(statusService).changeStep(1L, DocumentProcessingStep.CONTENT_GENERATING);
        order.verify(contentGenerationService).generate(10L);
    }
}
