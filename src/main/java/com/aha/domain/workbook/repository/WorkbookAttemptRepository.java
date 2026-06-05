package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.WorkbookAttempt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkbookAttemptRepository extends JpaRepository<WorkbookAttempt, Long> {

  @Query("""
        SELECT wa FROM WorkbookAttempt wa
        LEFT JOIN FETCH wa.workbookResult wr
        WHERE wa.userId = :userId
          AND wa.workbook.id IN :workbookIds
          AND wa.id = (
              SELECT MAX(wa2.id)
              FROM WorkbookAttempt wa2
              WHERE wa2.workbook.id = wa.workbook.id AND wa2.userId = :userId
          )
    """)
  List<WorkbookAttempt> findLatestAttempts(
      @Param("userId") Long userId,
      @Param("workbookIds") List<Long> workbookIds
  );


  Optional<WorkbookAttempt> findByWorkbook_IdAndUserId(Long workbookId, Long userId);
}
