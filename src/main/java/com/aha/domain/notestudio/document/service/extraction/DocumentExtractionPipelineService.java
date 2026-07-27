package com.aha.domain.notestudio.document.service.extraction;

import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.repository.SourceDocumentRepository;
import com.aha.domain.notestudio.document.service.chunk.DocumentChunkService;
import com.aha.domain.notestudio.document.service.extraction.model.ExtractedDocument;
import com.aha.domain.notestudio.document.service.extraction.model.ExtractedDocumentContext;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 업로드된 문서의 텍스트와 구조를 추출하고,
 * 추출 결과를 기반으로 문서 청크를 생성하는 파이프라인이다.
 *
 * 텍스트 추출과 청크 생성을 각각 분리하여
 * 상위 Worker가 실제 작업 경계에 맞게 처리 단계를 변경할 수 있도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExtractionPipelineService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentTextExtractionService documentTextExtractionService;
    private final DocumentChunkService documentChunkService;

    /**
     * 처리 그룹에 포함된 모든 문서에서 텍스트와 구조 블록을 추출한다.
     */
    public List<ExtractedDocumentContext> extractDocuments(
            Long processingGroupId
    ) {
        List<SourceDocument> sourceDocuments =
                getSourceDocuments(processingGroupId);

        List<ExtractedDocumentContext> extractedDocuments =
                new ArrayList<>(sourceDocuments.size());

        for (SourceDocument sourceDocument : sourceDocuments) {
            ExtractedDocument extractedDocument =
                    extractDocument(sourceDocument);

            extractedDocuments.add(
                    new ExtractedDocumentContext(
                            sourceDocument,
                            extractedDocument
                    )
            );
        }

        return List.copyOf(extractedDocuments);
    }

    /**
     * 추출된 문서 블록을 분석하여 청크를 생성하고 저장한다.
     */
    public void createChunks(
            List<ExtractedDocumentContext> extractedDocuments
    ) {
        validateExtractedDocuments(extractedDocuments);

        for (ExtractedDocumentContext context : extractedDocuments) {
            createDocumentChunks(context);
        }
    }

    private ExtractedDocument extractDocument(
            SourceDocument sourceDocument
    ) {
        log.info(
                "문서 텍스트 추출 시작. sourceDocumentId={}, fileName={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName()
        );

        ExtractedDocument extractedDocument =
                documentTextExtractionService.extract(
                        sourceDocument
                );

        log.info(
                "문서 텍스트 추출 완료. sourceDocumentId={}, blockCount={}",
                sourceDocument.getId(),
                extractedDocument.blocks().size()
        );

        return extractedDocument;
    }

    private void createDocumentChunks(
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

        int chunkCount =
                documentChunkService.createChunks(
                        sourceDocument,
                        extractedDocument.blocks()
                );

        log.info(
                "문서 청크 생성 완료. sourceDocumentId={}, chunkCount={}",
                sourceDocument.getId(),
                chunkCount
        );
    }

    private List<SourceDocument> getSourceDocuments(
            Long processingGroupId
    ) {
        validateProcessingGroupId(processingGroupId);

        List<SourceDocument> sourceDocuments =
                sourceDocumentRepository
                        .findAllByProcessingGroup_IdOrderByIdAsc(
                                processingGroupId
                        );

        if (sourceDocuments.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SOURCE_DOCUMENT_NOT_FOUND
            );
        }

        return sourceDocuments;
    }

    private void validateProcessingGroupId(
            Long processingGroupId
    ) {
        if (processingGroupId == null
                || processingGroupId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private void validateExtractedDocuments(
            List<ExtractedDocumentContext> extractedDocuments
    ) {
        if (extractedDocuments == null
                || extractedDocuments.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SOURCE_DOCUMENT_NOT_FOUND
            );
        }

        if (extractedDocuments.stream().anyMatch(context -> context == null)) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED
            );
        }
    }
}