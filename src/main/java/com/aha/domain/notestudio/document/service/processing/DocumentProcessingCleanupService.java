package com.aha.domain.notestudio.document.service.processing;

import com.aha.domain.notestudio.document.repository.DocumentChunkEmbeddingRepository;
import com.aha.domain.notestudio.document.repository.DocumentChunkRepository;
import com.aha.domain.notestudio.document.repository.DocumentScopeMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingCleanupService {

    private final DocumentScopeMappingRepository mappingRepository;
    private final DocumentChunkEmbeddingRepository embeddingRepository;
    private final DocumentChunkRepository chunkRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public CleanupResult cleanupForRestart(Long processingGroupId) {
        int deletedMappings = mappingRepository.deleteAllByProcessingGroupId(processingGroupId);
        int deletedEmbeddings = embeddingRepository.deleteAllByProcessingGroupId(processingGroupId);
        int deletedChunks = chunkRepository.deleteAllByProcessingGroupId(processingGroupId);

        log.info(
                "문서 재처리 잔재 삭제 완료. processingGroupId={}, mappings={}, embeddings={}, chunks={}",
                processingGroupId, deletedMappings, deletedEmbeddings, deletedChunks
        );

        return new CleanupResult(deletedMappings, deletedEmbeddings, deletedChunks);
    }

    public record CleanupResult(int mappings, int embeddings, int chunks) {
    }
}
