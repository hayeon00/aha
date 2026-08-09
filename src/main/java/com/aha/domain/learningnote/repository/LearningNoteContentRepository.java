package com.aha.domain.learningnote.repository;

import com.aha.domain.learningnote.entity.LearningNoteContent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface LearningNoteContentRepository extends JpaRepository<LearningNoteContent, Long> {

    void deleteAllByLearningNote_Id(Long learningNoteId);

    Optional<LearningNoteContent> findByLearningNote_IdAndExamScopeNode_Id(
            Long learningNoteId,
            Long examScopeNodeId
    );

    @Query("""
        select content
        from LearningNoteContent content
        join fetch content.examScopeNode scopeNode
        where content.learningNote.id = :learningNoteId
        order by scopeNode.depth asc, scopeNode.displayOrder asc, scopeNode.id asc
        """)
    List<LearningNoteContent> findAllByLearningNoteIdWithScopeNode(
            @Param("learningNoteId") Long learningNoteId
    );

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from LearningNoteContent content
        where content.learningNote.id = :learningNoteId
          and content.sourceType = com.aha.domain.learningnote.enums.LearningContentSourceType.DOCUMENT_BASED
        """)
    int deleteDocumentBasedByLearningNoteId(@Param("learningNoteId") Long learningNoteId);
}
