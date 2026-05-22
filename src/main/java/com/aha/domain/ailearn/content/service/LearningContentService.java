package com.aha.domain.ailearn.content.service;

import com.aha.domain.ailearn.content.dto.LearningContentBodyResponse;
import com.aha.domain.ailearn.content.dto.LearningContentResponse;
import com.aha.domain.ailearn.content.entity.LearningContent;
import com.aha.domain.ailearn.content.repository.LearningContentBodyRepository;
import com.aha.domain.ailearn.content.repository.LearningContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningContentService {

    private final LearningContentRepository learningContentRepository;
    private final LearningContentBodyRepository learningContentBodyRepository;

    public LearningContentResponse getLearningContent(Long scopeNodeId) {
        LearningContent content = learningContentRepository
                .findFirstByExamScopeNodeIdAndIsActiveTrueOrderByDisplayOrderAsc(scopeNodeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목차의 학습 콘텐츠가 없습니다."));

        List<LearningContentBodyResponse> bodies = learningContentBodyRepository
                .findByLearningContentIdAndIsActiveTrueOrderByDisplayOrderAsc(content.getId())
                .stream()
                .map(LearningContentBodyResponse::from)
                .toList();

        return LearningContentResponse.of(content, bodies);
    }
}