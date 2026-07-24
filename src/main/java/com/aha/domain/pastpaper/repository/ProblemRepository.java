package com.aha.domain.pastpaper.repository;

import com.aha.domain.pastpaper.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem,Long> {

}
