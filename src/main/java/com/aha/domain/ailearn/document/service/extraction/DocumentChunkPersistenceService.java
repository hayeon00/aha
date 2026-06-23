package com.aha.domain.ailearn.document.service.extraction;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import com.aha.domain.ailearn.document.repository.DocumentChunkRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentChunkPersistenceService {

    private final DocumentChunkRepository documentChunkRepository;

    @Transactional
    public void replaceChunks(
            DocumentProcessing processing,
            List<String> chunkTexts
    ) {
        validateInput(
                processing,
                chunkTexts
        );

        SourceDocument sourceDocument =
                processing.getSourceDocument();

        /*
         * 같은 문서를 재처리하는 경우
         * 기존 청크가 중복으로 남지 않도록 삭제한다.
         */
        documentChunkRepository
                .deleteAllBySourceDocument_Id(
                        sourceDocument.getId()
                );

        List<DocumentChunk> chunks =
                new ArrayList<>();

        for (int index = 0;
             index < chunkTexts.size();
             index++) {

            String chunkText =
                    chunkTexts.get(index);

            if (chunkText == null
                    || chunkText.isBlank()) {
                continue;
            }

            DocumentChunk chunk =
                    DocumentChunk.builder()
                            .sourceDocument(sourceDocument)
                            .processing(processing)
                            .chunkOrder(index + 1)
                            .contentType(
                                    DocumentChunkContentType.TEXT
                            )
                            .contentText(chunkText.trim())
                            .rawText(chunkText.trim())
                            .build();

            chunks.add(chunk);
        }

        if (chunks.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }

        documentChunkRepository.saveAll(chunks);

        log.info(
                "문서 청크 저장 완료. processingId={}, sourceDocumentId={}, chunkCount={}",
                processing.getId(),
                sourceDocument.getId(),
                chunks.size()
        );
    }

    private void validateInput(
            DocumentProcessing processing,
            List<String> chunkTexts
    ) {
        if (processing == null
                || processing.getSourceDocument() == null) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_PROCESSING_NOT_FOUND
            );
        }

        if (chunkTexts == null
                || chunkTexts.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }
    }
}