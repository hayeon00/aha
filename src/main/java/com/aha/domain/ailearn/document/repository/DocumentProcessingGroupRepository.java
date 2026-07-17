package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface DocumentProcessingGroupRepository extends JpaRepository<DocumentProcessingGroup, Long> {

    Optional<DocumentProcessingGroup> findByIdAndUserExam_User_Id(Long processingGroupId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select processingGroup
        from DocumentProcessingGroup processingGroup
        join fetch processingGroup.userExam userExam
        where processingGroup.id = :processingGroupId
          and userExam.user.id = :userId
    """)
    Optional<DocumentProcessingGroup> findOwnedByIdForUpdate(
            @Param("processingGroupId") Long processingGroupId,
            @Param("userId") Long userId
    );

    @Query("""
    select dpg
    from DocumentProcessingGroup dpg
    join fetch dpg.userExam ue
    join fetch ue.examVersion ev
    where dpg.id = :processingGroupId
    """)
    Optional<DocumentProcessingGroup> findByIdWithExamVersion(@Param("processingGroupId") Long processingGroupId);


    Optional<DocumentProcessingGroup>
    findTopByUserExam_IdAndUserExam_User_IdOrderByCreatedAtDesc(
            Long userExamId,
            Long userId
    );

    List<DocumentProcessingGroup>
    findAllByUserExam_User_IdAndStatusInOrderByCreatedAtDesc(
            Long userId,
            List<DocumentProcessingStatus> statuses
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update DocumentProcessingGroup processingGroup
           set processingGroup.status =
                   com.aha.domain.ailearn.document.enums.DocumentProcessingStatus.PROCESSING,
               processingGroup.currentStep =
                   com.aha.domain.ailearn.document.enums.DocumentProcessingStep.TEXT_EXTRACTING,
               processingGroup.startedAt = :startedAt,
               processingGroup.completedAt = null,
               processingGroup.errorMessage = null,
               processingGroup.updatedAt = :startedAt
         where processingGroup.id = :processingGroupId
           and processingGroup.status =
                   com.aha.domain.ailearn.document.enums.DocumentProcessingStatus.PENDING
           and processingGroup.currentStep =
                   com.aha.domain.ailearn.document.enums.DocumentProcessingStep.UPLOAD_COMPLETED
    """)
    int startProcessingIfReady(
            @Param("processingGroupId")
            Long processingGroupId,

            @Param("startedAt")
            LocalDateTime startedAt
    );
}
