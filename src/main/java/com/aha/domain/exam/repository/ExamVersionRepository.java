package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.entity.ExamVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamVersionRepository extends JpaRepository<ExamVersion, Long> {

    Optional<ExamVersion> findFirstByExam_IdAndStatusOrderByVersionNoDesc(
            Long examId,
            ExamVersionStatus status
    );

    List<ExamVersion> findAllByStatusOrderByExam_IdAscVersionNoDesc(ExamVersionStatus status);

    boolean existsByIdAndStatus(Long id, ExamVersionStatus status);
}