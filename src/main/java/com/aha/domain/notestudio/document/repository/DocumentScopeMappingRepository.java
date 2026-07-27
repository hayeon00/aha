package com.aha.domain.notestudio.document.repository;

import com.aha.domain.notestudio.document.entity.DocumentScopeMapping;
import com.aha.domain.notestudio.document.repository.projection.MappedTopicChunkProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DocumentScopeMappingRepository extends JpaRepository<DocumentScopeMapping, Long> {

    @Query("""
        select distinct mapping.examScopeNode.id
        from DocumentScopeMapping mapping
        where mapping.documentChunk.sourceDocument.id = :documentId
    """)
    Set<Long> findMappedTocIdsByDocumentId(@Param("documentId") Long documentId);

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from DocumentScopeMapping mapping
        where mapping.documentChunk.sourceDocument.processingGroup.id
              = :processingGroupId
        """)
    int deleteAllByProcessingGroupId(@Param("processingGroupId") Long processingGroupId);

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from DocumentScopeMapping mapping
        where mapping.documentChunk.sourceDocument.id = :documentId
        """)
    int deleteAllByDocumentId(@Param("documentId") Long documentId);

    void deleteAllByDocumentChunk_Id(Long documentChunkId);

    @Query("""
        select mapping
        from DocumentScopeMapping mapping
        join fetch mapping.documentChunk chunk
        join fetch chunk.sourceDocument sourceDocument
        join fetch sourceDocument.processingGroup processingGroup
        where processingGroup.userExam.id = :userExamId
          and processingGroup.userExam.user.id = :userId
          and mapping.examScopeNode.id = :examScopeNodeId
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
        join chunk.sourceDocument sourceDocument
        join mapping.examScopeNode scopeNode
        where sourceDocument.processingGroup.id = :processingGroupId
        order by
            scopeNode.id asc,
            mapping.rankNo asc,
            chunk.chunkOrder asc
    """)
    List<MappedTopicChunkProjection>
    findMappedTopicChunks(
            @Param("processingGroupId")
            Long processingGroupId
    );






}
