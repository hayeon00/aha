package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamScopeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExamScopeNodeRepository extends JpaRepository<ExamScopeNode, Long> {

    @Query("""
        SELECT n
        FROM ExamScopeNode n
        LEFT JOIN FETCH n.parent
        WHERE n.examVersion.id = :examVersionId
          AND n.isActive = true
        ORDER BY n.depth ASC, n.displayOrder ASC, n.id ASC
    """)
    List<ExamScopeNode> findActiveNodesByExamVersionId(Long examVersionId);
}