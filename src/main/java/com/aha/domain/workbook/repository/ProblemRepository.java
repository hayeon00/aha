package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem,Long> {

}
