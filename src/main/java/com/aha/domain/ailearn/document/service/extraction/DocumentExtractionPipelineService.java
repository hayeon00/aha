package com.aha.domain.ailearn.document.service.extraction;

import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.domain.ailearn.document.service.chunk.DocumentChunkService;
import com.aha.domain.ailearn.document.service.extraction.model.ExtractedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 PDF/DOC/DOCX/TXT
 → 텍스트 추출
 → 표 추출
 → OCR
 → 레이아웃 분석
 → DocumentBlock 생성
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExtractionPipelineService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentTextExtractionService documentTextExtractionService;
    private final DocumentChunkService documentChunkService;

    @Transactional
    public void extractDocuments(Long processingGroupId) {

        if (processingGroupId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<SourceDocument> sourceDocuments =
                sourceDocumentRepository.findAllByProcessingGroup_IdOrderByIdAsc(processingGroupId);

        if (sourceDocuments.isEmpty()) {
            throw new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND);
        }

        for (SourceDocument sourceDocument : sourceDocuments) {
            extractAndCreateChunks(sourceDocument);
        }
    }

    private void extractAndCreateChunks(SourceDocument sourceDocument) {

        log.info(
                "문서 추출 파이프라인 시작. sourceDocumentId={}, fileName={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName()
        );

        ExtractedDocument extractedDocument = documentTextExtractionService.extract(sourceDocument);

        int chunkCount = documentChunkService.createChunks(sourceDocument, extractedDocument.blocks());

        log.info(
                "문서 추출 파이프라인 완료. sourceDocumentId={}, chunkCount={}",
                sourceDocument.getId(),
                chunkCount
        );
    }
}