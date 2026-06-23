package com.aha.domain.ailearn.document.service.extraction;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.repository.DocumentProcessingRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 그룹에 속한 문서별로 다음 작업을 수행한다.
 *
 * 1. 실제 파일 조회
 * 2. 텍스트 추출
 * 3. 텍스트 정규화
 * 4. 청크 분할
 * 5. DocumentChunk 저장
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentExtractionService {

    private final DocumentProcessingRepository
            documentProcessingRepository;

    private final DocumentTextExtractionService
            documentTextExtractionService;

    private final DocumentChunkingService
            documentChunkingService;

    private final DocumentChunkPersistenceService
            documentChunkService;

    @Transactional
    public void extractDocuments(Long processingGroupId) {
        List<DocumentProcessing> processings =
                getDocumentProcessings(processingGroupId);

        for (DocumentProcessing processing : processings) {
            extractDocument(processing);
        }

        log.info(
                "처리 그룹 문서 추출 완료. processingGroupId={}, fileCount={}",
                processingGroupId,
                processings.size()
        );
    }

    private void extractDocument(
            DocumentProcessing processing
    ) {
        String extractedText =
                documentTextExtractionService.extract(
                        processing
                );

        List<String> chunks =
                documentChunkingService.split(
                        extractedText
                );

        documentChunkService.replaceChunks(
                processing,
                chunks
        );

        log.info(
                "문서 텍스트 추출 및 청크 저장 완료. processingId={}, sourceDocumentId={}, chunkCount={}",
                processing.getId(),
                processing.getSourceDocument().getId(),
                chunks.size()
        );
    }



    // ==================== 내부 Method =====================================

    private List<DocumentProcessing> getDocumentProcessings(
            Long processingGroupId
    ) {
        List<DocumentProcessing> processings =
                documentProcessingRepository
                        .findAllByProcessingGroup_IdOrderByIdAsc(
                                processingGroupId
                        );

        if (processings.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND
            );
        }

        return processings;
    }
}