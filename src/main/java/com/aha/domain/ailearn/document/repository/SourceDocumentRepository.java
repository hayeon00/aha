package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {

    List<SourceDocument> findAllByProcessingGroup_Id(Long processingGroupId);

    List<SourceDocument> findAllByProcessingGroup_IdOrderByIdAsc(Long processingGroupId);

}