package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.ExtractedContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExtractedContentRepository extends JpaRepository<ExtractedContent, Long> {

    List<ExtractedContent> findBySourceDocumentIdOrderByChunkOrderAsc(Long sourceDocumentId);

    List<ExtractedContent> findByExamScopeNodeIdOrderByChunkOrderAsc(Long examScopeNodeId);

    List<ExtractedContent> findBySourceDocumentIdAndIsUsedForLearningTrueOrderByChunkOrderAsc(Long sourceDocumentId);

    List<ExtractedContent> findBySourceDocumentIdAndIsUsedForRagTrueOrderByChunkOrderAsc(Long sourceDocumentId);
}