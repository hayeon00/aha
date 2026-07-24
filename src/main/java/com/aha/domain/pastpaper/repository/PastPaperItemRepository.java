package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.PastPaperItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PastPaperItemRepository extends JpaRepository<PastPaperItem, Long> {

    boolean existsByPastPaper_IdAndProblem_Id(Long pastPaperId,Long problemId);

    @Query("""
    select ppi from PastPaperItem ppi
    join fetch ppi.problem p
    join fetch p.examScopeNode esn
    join fetch esn.examPart ep
    where ppi.pastPaper.id = :paperId
""")
    List<PastPaperItem> findByPastPaper_IdWithProblemAndExamScopeNodeAndExamPart(Long paperId);

    @Query("""
    select ppi from PastPaperItem ppi
    join fetch ppi.problem
    where ppi.pastPaper.id = :paperId
""")
    List<PastPaperItem> findByPastPaper_IdWithProblem(Long paperId);
}
