package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentProcessingGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentProcessingGroupRepository extends JpaRepository<DocumentProcessingGroup, Long> {

    Optional<DocumentProcessingGroup> findByIdAndUserExam_User_Id(Long processingGroupId, Long userId);
}