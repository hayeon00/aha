package com.aha.domain.ailearn.conceptproblem.repository;

import com.aha.domain.ailearn.conceptproblem.entity.LearningProblemAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningProblemAttemptRepository extends JpaRepository<LearningProblemAttempt, Long> {

    List<LearningProblemAttempt> findByLearningSessionId(Long learningSessionId);

    boolean existsByLearningSessionId(Long learningSessionId);

    void deleteByLearningSessionId(Long learningSessionId);
}