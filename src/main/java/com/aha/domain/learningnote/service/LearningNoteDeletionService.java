package com.aha.domain.learningnote.service;

import com.aha.domain.document.entity.DocumentProcessing;
import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.repository.DocumentChunkEmbeddingRepository;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.repository.DocumentProcessingRepository;
import com.aha.domain.document.repository.DocumentScopeMappingRepository;
import com.aha.domain.document.repository.SourceDocumentRepository;
import com.aha.domain.document.service.storage.DocumentFileStorageService;
import com.aha.domain.learningnote.repository.LearningNoteContentRepository;
import com.aha.domain.learningnote.repository.LearningNoteRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class LearningNoteDeletionService {

    private final DocumentProcessingRepository documentProcessingRepository;
    private final LearningNoteContentRepository learningNoteContentRepository;
    private final DocumentScopeMappingRepository documentScopeMappingRepository;
    private final DocumentChunkEmbeddingRepository documentChunkEmbeddingRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final LearningNoteRepository learningNoteRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentFileStorageService documentFileStorageService;

    @Transactional
    public void deleteCompletedNote(Long userId, Long learningNoteId) {
        validateIds(userId, learningNoteId);

        DocumentProcessing processing = documentProcessingRepository
                .findCompletedOwnedDetail(learningNoteId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_NOTE_NOT_FOUND));

        SourceDocument sourceDocument = processing.getLearningNote().getSourceDocument();
        Long sourceDocumentId = sourceDocument.getId();
        String storageKey = sourceDocument.getStorageKey();

        learningNoteContentRepository.deleteAllByLearningNote_Id(learningNoteId);
        documentScopeMappingRepository.deleteAllByDocumentChunk_SourceDocument_Id(sourceDocumentId);
        documentChunkEmbeddingRepository.deleteAllByDocumentChunk_SourceDocument_Id(sourceDocumentId);
        documentChunkRepository.deleteAllBySourceDocument_Id(sourceDocumentId);
        documentProcessingRepository.deleteByLearningNote_Id(learningNoteId);
        learningNoteRepository.deleteById(learningNoteId);
        sourceDocumentRepository.deleteById(sourceDocumentId);

        deleteStoredFileAfterCommit(storageKey);
    }

    private void deleteStoredFileAfterCommit(String storageKey) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                documentFileStorageService.delete(storageKey);
            }
        });
    }

    private void validateIds(Long userId, Long learningNoteId) {
        if (userId == null || userId <= 0 || learningNoteId == null || learningNoteId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
