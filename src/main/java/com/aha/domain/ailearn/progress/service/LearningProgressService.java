package com.aha.domain.ailearn.progress.service;

import com.aha.domain.ailearn.progress.dto.response.LearningProgressSummaryResponse;
import com.aha.domain.ailearn.session.repository.LearningSessionRepository;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningProgressService {

    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final LearningSessionRepository learningSessionRepository;

    public LearningProgressSummaryResponse getProgressSummary(
            Long userId,
            Long examVersionId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다.");
        }

        long totalTopicCount =
                examScopeNodeRepository.countByExamVersion_IdAndIsLeafTrueAndIsActiveTrue(
                        examVersionId
                );

        long completedTopicCount =
                learningSessionRepository.countCompletedTopics(
                        userId,
                        examVersionId
                );

        double progressRate = totalTopicCount == 0
                ? 0.0
                : Math.round((completedTopicCount * 1000.0 / totalTopicCount)) / 10.0;

        return new LearningProgressSummaryResponse(
                examVersionId,
                totalTopicCount,
                completedTopicCount,
                progressRate
        );
    }
}