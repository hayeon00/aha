package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.UserAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAnswerRepository extends JpaRepository<UserAnswer,Long> {

    List<UserAnswer> findByWorkbookAttempt_Id(Long attemptId);

    Optional<UserAnswer> findByProblem_IdAndWorkbookAttempt_Id(Long problemId, Long attemptId);
}
