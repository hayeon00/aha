package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamPart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExamPartRepository extends JpaRepository<ExamPart,Long> {

    @Query("""
    select ep from ExamPart ep
    join fetch ep.examScopeNodes esn
    where ep.examVersion.id = :examVersionId
""")
    List<ExamPart> findByExamVersion_IdWithExamScopeNodes(Long examVersionId);
}
