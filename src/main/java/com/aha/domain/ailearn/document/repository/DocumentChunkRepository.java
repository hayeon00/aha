package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    void deleteAllBySourceDocument_Id(Long id);

    @Query("""
            select dc
            from DocumentChunk dc
            join dc.sourceDocument sd
            join sd.processingGroup pg
            where pg.id = :processingGroupId
            order by dc.chunkOrder asc
            """)
    List<DocumentChunk> findAllByProcessingGroupId(@Param("processingGroupId") Long processingGroupId);
}