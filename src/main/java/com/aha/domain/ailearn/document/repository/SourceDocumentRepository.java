package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select d from SourceDocument d
        join fetch d.processingGroup pg
        join fetch pg.userExam ue
        join fetch ue.user u
        join fetch ue.examVersion ev
        where d.id = :documentId and u.id = :userId
    """)
    Optional<SourceDocument> findOwnedByIdForUpdate(
            @Param("documentId") Long documentId, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"processingGroup", "processingGroup.userExam"})
    List<SourceDocument> findAllByProcessingGroup_UserExam_IdAndProcessingGroup_UserExam_User_IdOrderByIdDesc(
            Long userExamId, Long userId);

    List<SourceDocument> findAllByProcessingGroup_Id(Long processingGroupId);

    List<SourceDocument> findAllByProcessingGroup_IdOrderByIdAsc(Long processingGroupId);

}
