package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.ExamScopeNodeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamScopeNodeEmbeddingRepository extends JpaRepository<ExamScopeNodeEmbedding, Long> {

    List<ExamScopeNodeEmbedding> findAllByExamScopeNode_IdInAndEmbeddingModel(
            Collection<Long> examScopeNodeIds,
            String embeddingModel
    );

}