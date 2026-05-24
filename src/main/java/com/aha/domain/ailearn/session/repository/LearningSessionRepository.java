package com.aha.domain.ailearn.session.repository;

import com.aha.domain.ailearn.session.entity.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {

    @Query("""
        SELECT COUNT(DISTINCT ls.examScopeNodeId)
        FROM LearningSession ls
        JOIN ExamScopeNode esn ON esn.id = ls.examScopeNodeId
        WHERE ls.userId = :userId
          AND esn.examVersion.id = :examVersionId
          AND esn.isLeaf = true
          AND esn.isActive = true
          AND ls.status = 'COMPLETED'
    """)
    long countCompletedTopics(
            @Param("userId") Long userId,
            @Param("examVersionId") Long examVersionId
    );

    Optional<LearningSession> findByUserIdAndExamScopeNodeId(
            Long userId,
            Long examScopeNodeId
    );
}