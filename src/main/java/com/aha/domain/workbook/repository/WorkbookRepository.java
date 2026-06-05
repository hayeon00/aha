package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.Workbook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aha.domain.workbook.entity.WorkbookStatus;

public interface WorkbookRepository extends JpaRepository<Workbook, Long> {

  @Query("""
          SELECT w FROM Workbook w
          INNER JOIN w.examWorkbookType ewt
          INNER JOIN ewt.workbookType wt
          INNER JOIN ewt.exam e
          WHERE e.code = :examCode
          AND wt.code = :workbookTypeCode
          AND ewt.isActive = true
          AND w.status = :workbookStatus
          ORDER BY w.examYear DESC, w.no DESC
      """)
  List<Workbook> findReviewedWorkbooks(
      @Param("examCode") String examCode,
      @Param("workbookTypeCode") String workbookTypeCode,
      @Param("workbookStatus") WorkbookStatus workbookStatus
  );

  Optional<Workbook> findById(Long workbookId);

}

