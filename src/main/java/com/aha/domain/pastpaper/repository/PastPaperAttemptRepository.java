package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PastPaperAttemptRepository extends JpaRepository<PastPaperAttempt, Long> {

    @Modifying
    @Query(
        value = """
            INSERT INTO past_paper_attempt (
                user_id,
                past_paper_id,
                status,
                due_at
            )
            VALUES (
                :userId,
                :pastPaperId,
                'SOLVING',
                DATE_ADD(
                    CURRENT_TIMESTAMP(6),
                    INTERVAL :timeLimit SECOND
                )
            )
            ON DUPLICATE KEY UPDATE
                id = LAST_INSERT_ID(id)
        """,
        nativeQuery = true
    )
    void insertOrReuseSolvingAttempt(
        @Param("userId") Long userId,
        @Param("pastPaperId") Long pastPaperId,
        @Param("timeLimit") int timeLimit
    );

    @Query(
        value = "SELECT LAST_INSERT_ID()",
        nativeQuery=true
    )
    Long findLastInsertId();

    @Query("""
    select ppa from PastPaperAttempt ppa
    join fetch ppa.pastPaper pp
    join fetch pp.examVersion ev
    join fetch ev.parts
    where ppa.id = :attemptId
""")
    Optional<PastPaperAttempt> findByIdWithPastPaperAndExamVersionAndExamParts(Long attemptId);

    @Query(
        value = """
        select ppa
        from PastPaperAttempt ppa
        join fetch ppa.pastPaper pp
        join fetch pp.examVersion ev
        join fetch ev.exam
        where ppa.userId = :userId and ppa.status = :status
        """,
        countQuery = """
        select count(ppa)
        from PastPaperAttempt ppa
        where ppa.userId = :userId and ppa.status = :status
        """
    )
    Page<PastPaperAttempt> findAllByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") PastPaperAttemptStatus status,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT ppa FROM PastPaperAttempt ppa
    WHERE ppa.id = :attemptId
""")
    Optional<PastPaperAttempt> findByIdForUpdate(@Param("attemptId") Long attemptId);

    @Query("""
        SELECT attempt.id
        FROM PastPaperAttempt attempt
        WHERE attempt.status =
            com.aha.domain.pastpaper.enums.PastPaperAttemptStatus.SOLVING
          AND attempt.dueAt <= CURRENT_TIMESTAMP
        ORDER BY attempt.dueAt ASC
        """)
    List<Long> findExpiredSolvingAttemptIds(
        Pageable pageable
    );
}
