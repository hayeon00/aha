package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DocumentChunkEmbeddingRepository extends JpaRepository<DocumentChunkEmbedding, Long> {

    void deleteAllByDocumentChunk_SourceDocument_Id(Long sourceDocumentId);

    List<DocumentChunkEmbedding>
    findAllByDocumentChunk_IdInAndEmbeddingProviderAndEmbeddingModel(
            List<Long> documentChunkIds,
            String embeddingProvider,
            String embeddingModel
    );


}
