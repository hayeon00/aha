package com.aha.domain.exam.repository;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findAllByStatusOrderByIdAsc(ExamStatus status);
}