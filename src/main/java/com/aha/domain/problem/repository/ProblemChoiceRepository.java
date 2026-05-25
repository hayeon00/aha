package com.aha.domain.problem.repository;

import com.aha.domain.problem.entity.ProblemChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemChoiceRepository extends JpaRepository<ProblemChoice, Long> {

    Optional<ProblemChoice> findByProblem_IdAndChoiceNo(Long problemId, Integer choiceNo);
}