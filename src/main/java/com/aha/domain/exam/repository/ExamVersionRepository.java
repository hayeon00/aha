package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.ExamVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamVersionRepository extends JpaRepository<ExamVersion, Long> {

    Optional<ExamVersion> findFirstByExam_IdAndStatusOrderByVersionNoDesc(
            Long examId,
            String status
    );
}