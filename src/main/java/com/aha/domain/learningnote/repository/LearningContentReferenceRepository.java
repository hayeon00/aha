package com.aha.domain.learningnote.repository;

import com.aha.domain.learningnote.entity.LearningContentReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningContentReferenceRepository
        extends JpaRepository<LearningContentReference, Long> {

    void deleteAllByLearningNoteContent_Id(Long learningNoteContentId);

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from LearningContentReference reference
        where reference.learningNoteContent.learningNote.id = :learningNoteId
    """)
    int deleteAllByLearningNoteId(@Param("learningNoteId") Long learningNoteId);
}
