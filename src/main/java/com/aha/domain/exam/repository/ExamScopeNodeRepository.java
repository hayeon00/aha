package com.aha.domain.exam.repository;


import com.aha.domain.exam.entity.ExamScopeNode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExamScopeNodeRepository extends JpaRepository<ExamScopeNode, Long> {

    @Query("""
        SELECT esn
        FROM ExamScopeNode esn
        JOIN FETCH esn.examPart ep
        LEFT JOIN FETCH esn.parent p
        WHERE esn.examVersion.id = :examVersionId
          AND esn.isActive = true
        ORDER BY ep.displayOrder ASC, esn.depth ASC, esn.displayOrder ASC, esn.id ASC
    """)
    List<ExamScopeNode> findActiveNodesByExamVersionId(Long examVersionId);

    long countByExamVersion_IdAndIsLeafTrueAndIsActiveTrue(Long examVersionId);


}