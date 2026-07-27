package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.repository.projection.PastPaperWithAttemptProjection;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import com.aha.domain.pastpaper.enums.PastPaperStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PastPaperRepository extends JpaRepository<PastPaper,Long> {

    @Query("""
    select pp from PastPaper pp
    join fetch pp.examVersion ev
    join fetch ev.exam
    where pp.id = :pastPaperId
""")
    Optional<PastPaper> findByIdWithExamVersionAndExam(@Param("pastPaperId")Long pastPaperId);


    @Query("""
    select
        pp as pastPaper,
        attempt.id as solvingAttemptId,
        attempt.startedAt as attemptStartedAt
    from PastPaper pp
    left join PastPaperAttempt attempt
        on :userId is not null
       and attempt.pastPaper = pp
       and attempt.userId = :userId
       and attempt.status = :attemptStatus
    where pp.examVersion.id = :versionId
      and pp.status = :paperStatus
    order by pp.examDate desc
    """)
    List<PastPaperWithAttemptProjection> findAllWithSolvingAttempt(
        @Param("versionId") Long versionId,
        @Param("userId") Long userId,
        @Param("paperStatus") PastPaperStatus paperStatus,
        @Param("attemptStatus") PastPaperAttemptStatus attemptStatus
    );
}
