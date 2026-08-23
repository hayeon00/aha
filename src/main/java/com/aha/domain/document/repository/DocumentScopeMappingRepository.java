package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentScopeMapping;
import com.aha.domain.document.repository.projection.TopicMappingCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DocumentScopeMappingRepository
        extends JpaRepository<DocumentScopeMapping, Long> {

    void deleteAllByDocumentChunk_SourceDocument_Id(
            Long sourceDocumentId
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


    /**
     * LearningNote 안에서 각 시험 Topic에 매핑된 Chunk 수를 한 번에 집계한다.
     *
     * Topic별 count query 반복을 피하고 GROUP BY 한 번으로 Coverage 계산에 필요한
     * 데이터를 조회한다.
     */
    @Query("""
        select
            mapping.examScopeNode.id as scopeNodeId,
            count(distinct mapping.documentChunk.id) as mappingCount
        from DocumentScopeMapping mapping
        join mapping.documentChunk chunk
        where exists (
            select note.id
            from LearningNote note
            where note.id = :learningNoteId
              and note.sourceDocument = chunk.sourceDocument
        )
        group by mapping.examScopeNode.id
        """)
    List<TopicMappingCountProjection> findTopicMappingCountsByLearningNoteId(
            @Param("learningNoteId") Long learningNoteId
    );
}