package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.AiGeneratedLearningContentBody;
import com.aha.domain.ailearn.document.type.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiGeneratedLearningContentBodyRepository extends JpaRepository<AiGeneratedLearningContentBody, Long> {

    List<AiGeneratedLearningContentBody> findBySourceDocumentIdOrderByIdAsc(Long sourceDocumentId);

    List<AiGeneratedLearningContentBody> findByExamScopeNodeIdOrderByIdAsc(Long examScopeNodeId);

    List<AiGeneratedLearningContentBody> findBySourceDocumentIdAndReviewStatusOrderByIdAsc(
            Long sourceDocumentId,
            ReviewStatus reviewStatus
    );
}