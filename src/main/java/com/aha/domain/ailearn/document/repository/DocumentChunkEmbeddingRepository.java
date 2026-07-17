package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentChunkEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentChunkEmbeddingRepository extends JpaRepository<DocumentChunkEmbedding, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from DocumentChunkEmbedding embedding
        where embedding.documentChunk.sourceDocument.processingGroup.id = :processingGroupId
    """)
    int deleteAllByProcessingGroupId(@Param("processingGroupId") Long processingGroupId);

    List<DocumentChunkEmbedding> findAllByDocumentChunk_IdInAndEmbeddingModel(
            Collection<Long> documentChunkIds,
            String embeddingModel
    );


}
