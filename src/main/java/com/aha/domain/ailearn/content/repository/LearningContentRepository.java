package com.aha.domain.ailearn.content.repository;

import com.aha.domain.ailearn.content.entity.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {

    Optional<LearningContent> findFirstByExamScopeNodeIdAndIsActiveTrueOrderByDisplayOrderAsc(
            Long examScopeNodeId
    );
}