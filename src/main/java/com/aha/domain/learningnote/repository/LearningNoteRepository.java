package com.aha.domain.learningnote.repository;

import com.aha.domain.learningnote.entity.LearningNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LearningNoteRepository extends JpaRepository<LearningNote, Long> {















    @Query("""
        select note
        from LearningNote note
        join fetch note.sourceDocument
        where note.id = :learningNoteId
    """)
    Optional<LearningNote> findByIdWithSourceDocument(
            @Param("learningNoteId") Long learningNoteId
    );

    @Query("""
        select note
        from LearningNote note
        join fetch note.userExam userExam
        join fetch userExam.examVersion examVersion
        join fetch examVersion.exam
        where note.id = :learningNoteId
    """)
    Optional<LearningNote> findByIdWithExamVersion(
            @Param("learningNoteId") Long learningNoteId
    );

}
