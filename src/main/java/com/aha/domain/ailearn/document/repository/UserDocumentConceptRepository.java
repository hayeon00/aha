package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.UserDocumentConcept;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDocumentConceptRepository extends JpaRepository<UserDocumentConcept, Long> {
    @EntityGraph(attributePaths = "toc")
    Optional<UserDocumentConcept> findByUser_IdAndDocument_IdAndToc_Id(
            Long userId, Long documentId, Long tocId);

    @EntityGraph(attributePaths = "toc")
    List<UserDocumentConcept> findAllByUser_IdAndDocument_IdOrderByToc_DisplayOrderAsc(
            Long userId, Long documentId);
}
