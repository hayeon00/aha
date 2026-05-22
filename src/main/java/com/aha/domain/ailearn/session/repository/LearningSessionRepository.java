package com.aha.domain.ailearn.session.repository;

import com.aha.domain.ailearn.session.entity.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
}