package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.UserAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAnswerRepository extends JpaRepository<UserAnswer,Long> {

    @Query("""
    select ua from UserAnswer ua
    join fetch ua.problem p
    where ua.pastPaperAttempt.id = :attemptId
""")
    List<UserAnswer> findByPastPaperAttempt_IdWithProblem(@Param("attemptId")Long attemptId);

    @Query("""
    select ua from UserAnswer ua
    join fetch ua.problem p
    join fetch p.examScopeNode
    where ua.pastPaperAttempt.id = :attemptId
""")
    List<UserAnswer> findByPastPaperAttempt_IdWithProblemAndExamScopeNode(@Param("attemptId")Long attemptId);

    Optional<UserAnswer> findByPastPaperAttempt_IdAndProblem_Id(Long attemptId, Long problemId);

}
