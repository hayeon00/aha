package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.entity.ExamVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamVersionRepository extends JpaRepository<ExamVersion, Long> {

    Optional<ExamVersion> findFirstByExam_IdAndStatusOrderByVersionNoDesc(
            Long examId,
            ExamVersionStatus status
    );

    List<ExamVersion> findAllByStatusOrderByExam_IdAscVersionNoDesc(ExamVersionStatus status);

    boolean existsByIdAndStatus(Long id, ExamVersionStatus status);


    @Query("""
    select ev from ExamVersion ev
    join fetch ev.exam
    where ev.id = :versionId
""")
    Optional<ExamVersion> findByIdWithExam(@Param("versionId")Long versionId);
}