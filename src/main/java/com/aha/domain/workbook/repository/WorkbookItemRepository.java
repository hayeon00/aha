package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.WorkbookItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkbookItemRepository extends JpaRepository<WorkbookItem,Long> {

    @Query("""
    select distinct wi from WorkbookItem wi
     join fetch wi.problem p
      join fetch p.examScopeNode esn
        join fetch esn.examPart ep
        join fetch p.problemChoices pc
          where wi.workbook.id = :workbookId
            order by wi.sortOrder asc, pc.sortOrder asc
""")
    List<WorkbookItem> findByWorkbook_IdWithProblemAndExamScopeNodeAndExamPartAndProblemChoices(@Param("workbookId") Long workbookId);

    boolean existsByProblem_IdAndWorkbook_Id(Long problemId, Long workbookId);
}
