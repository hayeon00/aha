package com.aha.domain.notestudio.document.service;

import com.aha.domain.notestudio.document.entity.SourceDocument;
import com.aha.domain.notestudio.document.enums.DocumentProcessingStatus;
import com.aha.domain.notestudio.document.repository.DocumentChunkEmbeddingRepository;
import com.aha.domain.notestudio.document.repository.DocumentChunkRepository;
import com.aha.domain.notestudio.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.notestudio.document.repository.SourceDocumentRepository;
import com.aha.domain.notestudio.document.repository.UserDocumentConceptRepository;
import com.aha.domain.notestudio.document.service.upload.DocumentFileStorageService;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final SourceDocumentRepository sourceDocumentRepository;
    private final UserDocumentConceptRepository userDocumentConceptRepository;
    private final DocumentScopeMappingRepository documentScopeMappingRepository;
    private final DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentFileStorageService documentFileStorageService;

    @Transactional
    public void delete(Long userId, Long documentId) {
        if (userId == null || userId <= 0 || documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        SourceDocument sourceDocument = sourceDocumentRepository.findOwnedByIdForUpdate(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND));

        DocumentProcessingStatus processingStatus =
                sourceDocument.getProcessingGroup().getStatus();
        if (processingStatus == DocumentProcessingStatus.PENDING
                || processingStatus == DocumentProcessingStatus.PROCESSING) {
            throw new BusinessException(ErrorCode.INVALID_DOCUMENT_PROCESSING_STATUS);
        }

        String storageKey = sourceDocument.getStorageKey();

        userDocumentConceptRepository.deleteAllByDocument_Id(documentId);
        documentScopeMappingRepository.deleteAllByDocumentId(documentId);
        documentChunkEmbeddingRepository.deleteAllByDocumentId(documentId);
        documentChunkRepository.deleteAllBySourceDocument_Id(documentId);
        sourceDocumentRepository.delete(sourceDocument);
        sourceDocumentRepository.flush();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        documentFileStorageService.deleteStoredFile(storageKey);
                    }
                }
        );
    }
}
