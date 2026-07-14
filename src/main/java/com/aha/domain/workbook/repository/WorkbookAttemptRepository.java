package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.enums.AttemptStatus;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkbookAttemptRepository extends JpaRepository<WorkbookAttempt,Long> {


    @Query("""
    select wa from WorkbookAttempt wa
      where wa.userId = :userId and wa.workbook.id = :workbookId and wa.status = :attemptStatus
""")
    Optional<WorkbookAttempt> findByWorkbookIdAndUserIdStatus(
        @Param("workbookId") Long workbookId,@Param("userId") Long userId, @Param("attemptStatus")AttemptStatus attemptStatus);

    @Query("""
    select wa from WorkbookAttempt wa
     join fetch wa.workbook w
      join fetch w.examVersion ev
        join fetch ev.exam e
          where w.id = :workbookId and wa.userId = :userId
""")
    Optional<WorkbookAttempt> findByWorkbookIdAndUserIdWithWorkbookAndExamVersionAndExam(@Param("workbookId")Long workbookId, @Param("userId") Long userId);

    @Query("""
    select wa from WorkbookAttempt wa
     join fetch wa.workbook w
      join fetch w.workbookType wt
      join fetch w.examVersion ev
        join fetch ev.exam e
          where wa.id = :attemptId and wa.userId = :userId
""")
    Optional<WorkbookAttempt> findByIdWithWorkbookAndWorkbookTypeExamVersionAndExam(@Param("attemptId")Long attemptId, @Param("userId")Long userId);
}
