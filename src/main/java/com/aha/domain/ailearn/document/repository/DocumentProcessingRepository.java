package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.DocumentProcessing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentProcessingRepository extends JpaRepository<DocumentProcessing, Long> {

    List<DocumentProcessing> findAllByProcessingGroup_IdOrderByIdAsc(Long processingGroupId);
}