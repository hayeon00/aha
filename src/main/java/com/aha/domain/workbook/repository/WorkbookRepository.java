package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.Workbook;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkbookRepository extends JpaRepository<Workbook,Long> {
    @Query("""
    select w from Workbook w
     join fetch w.examVersion ev
      left join fetch w.pastExamWorkbook pew
      where w.id = :workbookId
""")
    Optional<Workbook> findByIdWithExamVersionAndPastExamWorkBook(@Param("workbookId") Long workbookId);
}
