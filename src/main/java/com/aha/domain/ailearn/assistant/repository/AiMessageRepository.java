package com.aha.domain.ailearn.assistant.repository;

import com.aha.domain.ailearn.assistant.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    List<AiMessage> findByLearningSessionIdOrderByCreatedAtAsc(Long learningSessionId);
}