package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PastPaperAttemptRepository extends JpaRepository<PastPaperAttempt, Long> {

    @Query("""
    select ppa from PastPaperAttempt ppa
    join fetch ppa.pastPaper
    where ppa.id = :attemptId
""")
    Optional<PastPaperAttempt> findByIdWithPastPaper(Long attemptId);

    @Query("""
            select ppa from PastPaperAttempt ppa
            join fetch ppa.pastPaper pp
            where pp.id = :paperId and ppa.userId = :userId and ppa.status = :status
        """)
    Optional<PastPaperAttempt> findByPastPaper_IdAndUserIdAndStatus(@Param("paperId") Long paperId,
        @Param("userId") Long userId,
        @Param("status") PastPaperAttemptStatus status);

    @Query("""
    select ppa from PastPaperAttempt ppa
    join fetch ppa.pastPaper pp
    join fetch pp.examVersion ev
    join fetch ev.parts
    where ppa.id = :attemptId
""")
    Optional<PastPaperAttempt> findByIdWithPastPaperAndExamVersionAndExamParts(Long attemptId);
}
