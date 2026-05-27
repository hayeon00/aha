package com.aha.domain.ailearn.document.repository;
import com.aha.domain.ailearn.document.entity.LearningSourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningSourceDocumentRepository extends JpaRepository<LearningSourceDocument, Long> {
}