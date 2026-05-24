package com.aha.domain.problem.repository;

import com.aha.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("""
        SELECT DISTINCT p
        FROM Problem p
        JOIN FETCH p.choices c
        WHERE p.examScopeNode.id = :examScopeNodeId
          AND p.sourceType = 'SEED'
          AND p.isActive = true
        ORDER BY p.id ASC
    """)
    List<Problem> findActiveConceptProblemsWithChoices(
            @Param("examScopeNodeId") Long examScopeNodeId
    );

    @Query("""
    SELECT p
    FROM Problem p
    WHERE p.examScopeNode.id = :examScopeNodeId
      AND p.id IN :problemIds
      AND p.sourceType = 'SEED'
      AND p.isActive = true
""")
    List<Problem> findActiveConceptProblemsByIds(
            @Param("examScopeNodeId") Long examScopeNodeId,
            @Param("problemIds") List<Long> problemIds
    );


}