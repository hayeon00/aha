package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ExamScopeNodeRepository extends JpaRepository<ExamScopeNode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM ExamScopeNode n LEFT JOIN FETCH n.parent WHERE n.id = :id")
    Optional<ExamScopeNode> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT n
        FROM ExamScopeNode n
        LEFT JOIN FETCH n.parent
        WHERE n.examVersion.id = :examVersionId
          AND n.isActive = true
          AND n.nodeType IN :nodeTypes
        ORDER BY n.depth ASC, n.displayOrder ASC, n.id ASC
    """)
    List<ExamScopeNode> findActiveNodesByExamVersionIdAndNodeTypes(
            @Param("examVersionId") Long examVersionId,
            @Param("nodeTypes") List<ExamScopeNodeType> nodeTypes
    );


    List<ExamScopeNode> findAllByExamVersion_IdAndIsLeafTrueAndIsActiveTrueOrderByDepthAscDisplayOrderAsc(Long examVersionId);

    List<ExamScopeNode> findAllByExamPart_IdInAndNodeTypeAndIsActiveTrueOrderByDisplayOrderAsc(List<Long> partIds , ExamScopeNodeType examScopeNodeType);

}
