package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}