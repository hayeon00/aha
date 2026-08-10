package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.repository.projection.MappedTopicChunkProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DocumentScopeMappingRepository extends JpaRepository<DocumentScopeMapping, Long> {

    void deleteAllByDocumentChunk_SourceDocument_Id(Long sourceDocumentId);

    @Query("""
        select distinct mapping.examScopeNode.id
        from DocumentScopeMapping mapping
        where mapping.documentChunk.sourceDocument.id = :documentId
        """)
    Set<Long> findMappedTocIdsByDocumentId(
            @Param("documentId") Long documentId
    );

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from DocumentScopeMapping mapping
        where mapping.documentChunk.sourceDocument in (
            select note.sourceDocument
            from LearningNote note
            where note.id = :learningNoteId
        )
        """)
    int deleteAllByLearningNoteId(
            @Param("learningNoteId") Long learningNoteId
    );

    @Query("""
        select mapping
        from DocumentScopeMapping mapping
        join fetch mapping.documentChunk chunk
        join fetch chunk.sourceDocument sourceDocument
        where mapping.examScopeNode.id = :examScopeNodeId
          and exists (
              select note.id
              from LearningNote note
              where note.sourceDocument = sourceDocument
                and note.userExam.id = :userExamId
                and note.userExam.user.id = :userId
          )
        order by chunk.chunkOrder asc, mapping.rankNo asc
        """)
    List<DocumentScopeMapping> findMappedChunks(
            @Param("userId") Long userId,
            @Param("userExamId") Long userExamId,
            @Param("examScopeNodeId") Long examScopeNodeId
    );

    @Query("""
        select
            scopeNode.id as topicId,
            scopeNode.title as topicTitle,
            chunk.id as chunkId,
            chunk.chunkOrder as chunkOrder,
            chunk.contentText as chunkText,
            chunk.contentType as chunkType
        from DocumentScopeMapping mapping
        join mapping.documentChunk chunk
        join mapping.examScopeNode scopeNode
        where exists (
            select note.id
            from LearningNote note
            where note.id = :learningNoteId
              and note.sourceDocument = chunk.sourceDocument
        )
        order by
            scopeNode.id asc,
            mapping.rankNo asc,
            chunk.chunkOrder asc
        """)
    List<MappedTopicChunkProjection> findMappedTopicChunks(
            @Param("learningNoteId") Long learningNoteId
    );

}
