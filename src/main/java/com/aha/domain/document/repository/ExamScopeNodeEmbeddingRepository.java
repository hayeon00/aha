package com.aha.domain.document.repository;

import com.aha.domain.document.entity.ExamScopeNodeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExamScopeNodeEmbeddingRepository extends JpaRepository<ExamScopeNodeEmbedding, Long> {

    List<ExamScopeNodeEmbedding> findAllByExamScopeNode_IdInAndEmbeddingModel(
            Collection<Long> examScopeNodeIds,
            String embeddingModel
    );

}