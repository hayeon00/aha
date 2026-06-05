package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.WorkbookItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkbookItemRepository extends JpaRepository<WorkbookItem, Long> {


  @Query("""
  SELECT DISTINCT wi
  FROM WorkbookItem wi
    JOIN FETCH wi.problem p
    LEFT JOIN FETCH p.choices pc
  WHERE wi.workbook.id = :workbookId
  ORDER BY wi.itemNo ASC
  """)
  List<WorkbookItem> findItemsWithProblemAndChoicesByWorkbookId(
      @Param("workbookId") Long workbookId
  );

}
