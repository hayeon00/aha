package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.UserAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAnswerRepository extends JpaRepository<UserAnswer,Long> {

    List<UserAnswer> findByWorkbookAttempt_Id(Long attemptId);

    Optional<UserAnswer> findByProblem_IdAndWorkbookAttempt_Id(Long problemId, Long attemptId);

    int countByWorkbookAttempt_Id(Long attemptId);

    @Query("""
    select ua from UserAnswer ua
    join fetch ua.problem p
    join fetch p.examScopeNode esn
    where ua.workbookAttempt.id = :attemptId
""")
    List<UserAnswer> findByAttempt_IdWithProblemAndExamScopeNode(@Param("attemptId")Long attemptId);
}
