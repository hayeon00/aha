package com.aha.domain.ailearn.assistant.repository;

import com.aha.domain.ailearn.assistant.entity.AiReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiReferenceRepository extends JpaRepository<AiReference, Long> {

    List<AiReference> findByAiMessageIdOrderByDisplayOrderAsc(Long aiMessageId);
}
