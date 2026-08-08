package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DocumentChunkEmbeddingRepository extends JpaRepository<DocumentChunkEmbedding, Long> {

    List<DocumentChunkEmbedding> findAllByDocumentChunk_IdInAndEmbeddingModel(
            Collection<Long> documentChunkIds,
            String embeddingModel
    );


}
