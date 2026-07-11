package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.PastExamWorkbook;
import com.aha.domain.workbook.entity.WorkbookStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PastExamWorkbookRepository extends JpaRepository<PastExamWorkbook,Long> {

    @Query("""
    select pew from PastExamWorkbook pew
     join fetch pew.workbook w
      join fetch w.examVersion e
       where e.id = :examVersionId
        and w.status = :workbookStatus
         and pew.isReviewed = true
          order by pew.examDate desc
 """)
    List<PastExamWorkbook> findPastExamWorkbookByExamVersion_Id(Long examVersionId,
        WorkbookStatus workbookStatus);
}
