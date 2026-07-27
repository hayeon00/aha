package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from DocumentChunk chunk
        where chunk.sourceDocument.processingGroup.id = :processingGroupId
    """)
    int deleteAllByProcessingGroupId(@Param("processingGroupId") Long processingGroupId);

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

    @Query("""
            select chunk
            from DocumentChunk chunk
            join fetch chunk.sourceDocument document
            join fetch document.processingGroup processingGroup
            join fetch processingGroup.userExam userExam
            join fetch userExam.examVersion
            where chunk.id = :chunkId
              and userExam.user.id = :userId
            """)
    java.util.Optional<DocumentChunk> findOwnedChunk(
            @Param("userId") Long userId,
            @Param("chunkId") Long chunkId
    );
}
