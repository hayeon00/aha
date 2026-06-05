package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.ExamWorkbookType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ExamWorkbookTypeRepository extends JpaRepository<ExamWorkbookType, Long> {

  Optional<ExamWorkbookType> findByExam_IdAndWorkbookType_IdAndIsActiveTrue(Long examId,
      Long workbookTypeId);

  @Query("""
      SELECT ewt FROM ExamWorkbookType ewt
      INNER JOIN FETCH ewt.workbookType wt
      WHERE ewt.exam.id = :examId
      AND ewt.isActive=true
            ORDER BY wt.displayOrder ASC
      """)
  List<ExamWorkbookType> findByExamIdAndIsActiveTrueWithWorkbookType(@Param("examId") Long examId);

}
