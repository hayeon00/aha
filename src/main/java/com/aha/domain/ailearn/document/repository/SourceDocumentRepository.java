package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {
}