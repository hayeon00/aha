package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentScopeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentScopeMappingRepository
        extends JpaRepository<DocumentScopeMapping, Long> {

    void deleteAllByDocumentChunk_SourceDocument_Id(
            Long sourceDocumentId
    );

    long countByDocumentChunk_SourceDocument_Id(
            Long sourceDocumentId
    );

    @Query("""
        select dsm
        from DocumentScopeMapping dsm
        join fetch dsm.documentChunk dc
        join fetch dsm.examScopeNode esn
        where dc.processing.processingGroup.id = :processingGroupId
        order by esn.id asc, dc.chunkOrder asc
        """)
    List<DocumentScopeMapping> findAllByProcessingGroupId(
            @Param("processingGroupId") Long processingGroupId
    );

    List<DocumentScopeMapping>
    findAllByDocumentChunk_SourceDocument_IdOrderByDocumentChunk_ChunkOrderAsc(
            Long sourceDocumentId
    );

    List<DocumentScopeMapping>
    findAllByExamScopeNode_IdOrderByDocumentChunk_ChunkOrderAsc(
            Long examScopeNodeId
    );
}