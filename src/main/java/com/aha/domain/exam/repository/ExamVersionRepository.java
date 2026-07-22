package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.entity.ExamVersionStatus;
import com.aha.domain.exam.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExamVersionRepository
        extends JpaRepository<ExamVersion, Long> {

    Optional<ExamVersion>
    findFirstByExam_IdAndStatusOrderByVersionNoDesc(
            Long examId,
            ExamVersionStatus status
    );

    List<ExamVersion>
    findAllByStatusOrderByExam_IdAscVersionNoDesc(
            ExamVersionStatus status
    );

    boolean existsByIdAndStatus(
            Long id,
            ExamVersionStatus status
    );

    @Query("""
        SELECT ev
        FROM ExamVersion ev
        JOIN FETCH ev.exam e
        WHERE e.id IN :examIds
          AND e.status = :examStatus
          AND ev.status = :versionStatus
          AND ev.versionNo = (
              SELECT MAX(ev2.versionNo)
              FROM ExamVersion ev2
              WHERE ev2.exam.id = e.id
                AND ev2.status = :versionStatus
          )
        ORDER BY e.id ASC
    """)
    List<ExamVersion> findLatestActiveVersionsByExamIds(
            @Param("examIds")
            Set<Long> examIds,

            @Param("examStatus")
            ExamStatus examStatus,

            @Param("versionStatus")
            ExamVersionStatus versionStatus
    );
}
