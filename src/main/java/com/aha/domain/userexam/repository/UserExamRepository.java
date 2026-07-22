package com.aha.domain.userexam.repository;

import com.aha.domain.userexam.entity.UserExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserExamRepository extends JpaRepository<UserExam, Long> {

    boolean existsByUser_IdAndExamVersion_Id(Long userId, Long examVersionId);

    @Query("""
        SELECT ue
        FROM UserExam ue
        JOIN FETCH ue.examVersion ev
        JOIN FETCH ev.exam e
        WHERE ue.user.id = :userId
        ORDER BY e.id ASC, ev.versionNo DESC
    """)
    List<UserExam> findAllWithExamByUserId(Long userId);

    @Query("""
        SELECT ue
        FROM UserExam ue
        JOIN FETCH ue.examVersion ev
        JOIN FETCH ev.exam e
        WHERE ue.user.id = :userId
          AND ue.isHidden = false
        ORDER BY e.id ASC, ev.versionNo DESC
    """)
    List<UserExam> findVisibleWithExamByUserId(Long userId);

    Optional<UserExam> findByIdAndUser_Id(Long userExamId, Long userId);

    List<UserExam> findAllByUser_Id(Long userId);

    @Query("""
        SELECT ue.examVersion.exam.id
        FROM UserExam ue
        WHERE ue.user.id = :userId
    """)
    Set<Long> findExamIdsByUserId(Long userId);

}
