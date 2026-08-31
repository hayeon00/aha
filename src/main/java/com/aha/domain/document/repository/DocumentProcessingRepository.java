package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentProcessing;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface DocumentProcessingRepository extends JpaRepository<DocumentProcessing, Long> {
















    void deleteByLearningNote_Id(Long learningNoteId);

    @Query("""
        select processing
        from DocumentProcessing processing
        join fetch processing.learningNote note
        join fetch note.sourceDocument
        join fetch note.userExam userExam
        join fetch userExam.examVersion examVersion
        join fetch examVersion.exam
        where note.userExam.user.id = :userId
          and note.status = com.aha.domain.learningnote.enums.LearningNoteStatus.READY
          and processing.status = com.aha.domain.document.enums.DocumentProcessingStatus.COMPLETED
          and processing.attemptNo = (
              select max(latest.attemptNo)
              from DocumentProcessing latest
              where latest.learningNote = note
                and latest.status = com.aha.domain.document.enums.DocumentProcessingStatus.COMPLETED
          )
        order by processing.completedAt desc, note.id desc
    """)
    List<DocumentProcessing> findCompletedOwnedByUserId(@Param("userId") Long userId);

    @Query("""
        select processing
        from DocumentProcessing processing
        join fetch processing.learningNote note
        join fetch note.sourceDocument
        join fetch note.userExam userExam
        join fetch userExam.examVersion examVersion
        join fetch examVersion.exam
        where processing.learningNote.id = :learningNoteId
          and userExam.user.id = :userId
          and note.status = com.aha.domain.learningnote.enums.LearningNoteStatus.READY
          and processing.status = com.aha.domain.document.enums.DocumentProcessingStatus.COMPLETED
          and processing.attemptNo = (
              select max(latest.attemptNo)
              from DocumentProcessing latest
              where latest.learningNote = note
                and latest.status = com.aha.domain.document.enums.DocumentProcessingStatus.COMPLETED
          )
    """)
    Optional<DocumentProcessing> findCompletedOwnedDetail(
            @Param("learningNoteId") Long learningNoteId,
            @Param("userId") Long userId
    );

    @Query("""
        select processing
        from DocumentProcessing processing
        join fetch processing.learningNote note
        join fetch note.sourceDocument
        where processing.id = :processingId
    """)
    Optional<DocumentProcessing> findByIdWithLearningNoteAndSourceDocument(
            @Param("processingId") Long processingId
    );

    @Query("""
        select processing
        from DocumentProcessing processing
        join fetch processing.learningNote note
        where processing.id = :processingId
          and note.userExam.user.id = :userId
    """)
    Optional<DocumentProcessing> findOwnedById(
            @Param("processingId") Long processingId,
            @Param("userId") Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select processing
        from DocumentProcessing processing
        where processing.id = :processingId
    """)
    Optional<DocumentProcessing> findByIdForUpdate(
            @Param("processingId") Long processingId
    );
}
