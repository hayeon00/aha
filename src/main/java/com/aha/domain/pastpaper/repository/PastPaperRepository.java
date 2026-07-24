package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.PastPaper;
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

    List<PastPaper> findByExamVersion_IdAndStatus(Long versionId, PastPaperStatus status);

}
