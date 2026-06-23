package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository
        extends JpaRepository<DocumentChunk, Long> {

    void deleteAllBySourceDocument_Id(
            Long sourceDocumentId
    );

    List<DocumentChunk>
    findAllBySourceDocument_IdOrderByChunkOrderAsc(
            Long sourceDocumentId
    );
}