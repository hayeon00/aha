package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStatus;
import com.aha.domain.ailearn.document.enums.DocumentProcessingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DocumentProcessingGroupRepository extends JpaRepository<DocumentProcessingGroup, Long> {

    Optional<DocumentProcessingGroup> findByIdAndUserExam_User_Id(Long processingGroupId, Long userId);

    @Query("""
    select dpg
    from DocumentProcessingGroup dpg
    join fetch dpg.userExam ue
    join fetch ue.examVersion ev
    where dpg.id = :processingGroupId
    """)
    Optional<DocumentProcessingGroup> findByIdWithExamVersion(@Param("processingGroupId") Long processingGroupId);

    @Query("""
    select processingGroup
    from DocumentProcessingGroup processingGroup
    join fetch processingGroup.userExam userExam
    join fetch userExam.user user
    where processingGroup.id = :processingGroupId
    """)
    Optional<DocumentProcessingGroup> findByIdWithUserExamAndUser(@Param("processingGroupId") Long processingGroupId);

    Optional<DocumentProcessingGroup>
    findTopByUserExam_IdAndUserExam_User_IdOrderByCreatedAtDesc(
            Long userExamId,
            Long userId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update DocumentProcessingGroup processingGroup
               set processingGroup.status = :processingStatus,
                   processingGroup.currentStep = :currentStep,
                   processingGroup.progressRate = :progressRate,
                   processingGroup.startedAt = :startedAt,
                   processingGroup.updatedAt = :updatedAt
             where processingGroup.id = :processingGroupId
               and processingGroup.status = :pendingStatus
            """)
    int startProcessingIfPending(
            @Param("processingGroupId") Long processingGroupId,
            @Param("pendingStatus") DocumentProcessingStatus pendingStatus,
            @Param("processingStatus") DocumentProcessingStatus processingStatus,
            @Param("currentStep") DocumentProcessingStep currentStep,
            @Param("progressRate") int progressRate,
            @Param("startedAt") LocalDateTime startedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}