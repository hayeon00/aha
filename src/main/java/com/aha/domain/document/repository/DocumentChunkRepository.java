package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    void deleteAllBySourceDocument_Id(Long id);

    List<DocumentChunk> findAllBySourceDocument_IdOrderByChunkOrderAsc(
            Long sourceDocumentId
    );

    @Query("""
            select chunk
            from DocumentChunk chunk,
                 LearningNote note
            where note.id = :learningNoteId
              and chunk.sourceDocument = note.sourceDocument
            order by chunk.chunkOrder asc
            """)
    List<DocumentChunk> findAllByLearningNoteId(
            @Param("learningNoteId") Long learningNoteId
    );

}
