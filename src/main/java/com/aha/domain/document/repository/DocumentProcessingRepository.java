package com.aha.domain.document.repository;

import com.aha.domain.document.entity.DocumentProcessing;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentProcessingRepository extends JpaRepository<DocumentProcessing, Long> {

    Optional<DocumentProcessing> findByLearningNote_Id(Long learningNoteId);

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
