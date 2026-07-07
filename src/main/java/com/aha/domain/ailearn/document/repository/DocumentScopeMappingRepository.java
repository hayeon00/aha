package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentScopeMappingRepository extends JpaRepository<DocumentScopeMapping, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from DocumentScopeMapping mapping
        where mapping.documentChunk.sourceDocument.processingGroup.id
              = :processingGroupId
        """)
    int deleteAllByProcessingGroupId(@Param("processingGroupId") Long processingGroupId);

    @Query("""
    select mapping
    from DocumentScopeMapping mapping
    join fetch mapping.documentChunk chunk
    join fetch chunk.sourceDocument sourceDocument
    join fetch mapping.examScopeNode scopeNode
    where sourceDocument.processingGroup.userExam.id = :userExamId
      and scopeNode.id = :examScopeNodeId
      and scopeNode.isLeaf = true
      and scopeNode.isActive = true
    order by mapping.confidenceScore desc,
             chunk.chunkOrder asc
    """)
    List<DocumentScopeMapping> findAllByUserExamIdAndExamScopeNodeId(
            @Param("userExamId") Long userExamId,
            @Param("examScopeNodeId") Long examScopeNodeId
    );

    @Query("""
    select distinct scopeNode.id
    from DocumentScopeMapping mapping
    join mapping.examScopeNode scopeNode
    where mapping.documentChunk.sourceDocument.processingGroup.id
          = :processingGroupId
      and scopeNode.isLeaf = true
      and scopeNode.isActive = true
    order by scopeNode.id asc
    """)
    List<Long> findDistinctExamScopeNodeIdsByProcessingGroupId(@Param("processingGroupId") Long processingGroupId);


}