package com.aha.domain.ailearn.content.repository;

import com.aha.domain.ailearn.content.entity.LearningContentBody;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningContentBodyRepository extends JpaRepository<LearningContentBody, Long> {

    List<LearningContentBody> findByLearningContentIdAndIsActiveTrueOrderByDisplayOrderAsc(
            Long learningContentId
    );
}