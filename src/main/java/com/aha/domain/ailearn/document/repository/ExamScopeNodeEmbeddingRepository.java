package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.ExamScopeNodeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamScopeNodeEmbeddingRepository extends JpaRepository<ExamScopeNodeEmbedding, Long> {

    Optional<ExamScopeNodeEmbedding> findByExamScopeNode_IdAndEmbeddingModel(
            Long examScopeNodeId,
            String embeddingModel
    );

    List<ExamScopeNodeEmbedding> findAllByExamScopeNode_IdInAndEmbeddingModel(
            Collection<Long> examScopeNodeIds,
            String embeddingModel
    );

    List<ExamScopeNodeEmbedding> findAllByExamScopeNode_ExamVersion_IdAndEmbeddingModel(
            Long examVersionId,
            String embeddingModel
    );

    boolean existsByExamScopeNode_IdAndEmbeddingModelAndEmbeddingTextHash(
            Long examScopeNodeId,
            String embeddingModel,
            String embeddingTextHash
    );

    void deleteAllByExamScopeNode_IdIn(Collection<Long> examScopeNodeIds);
}