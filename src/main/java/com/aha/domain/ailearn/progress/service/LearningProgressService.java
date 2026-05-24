package com.aha.domain.ailearn.progress.service;

import com.aha.domain.ailearn.progress.dto.response.LearningProgressSummaryResponse;
import com.aha.domain.ailearn.progress.dto.response.LearningTopicProgressResponse;
import com.aha.domain.ailearn.session.entity.LearningSession;
import com.aha.domain.ailearn.session.repository.LearningSessionRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningProgressService {

    private static final String STATUS_NOT_STARTED = "NOT_STARTED";

    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final LearningSessionRepository learningSessionRepository;

    public LearningProgressSummaryResponse getProgressSummary(
            Long userId,
            Long examVersionId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다.");
        }

        List<ExamScopeNode> topicNodes =
                examScopeNodeRepository.findByExamVersion_IdAndIsLeafTrueAndIsActiveTrueOrderByDisplayOrderAsc(
                        examVersionId
                );

        List<Long> topicNodeIds = topicNodes.stream()
                .map(ExamScopeNode::getId)
                .toList();

        List<LearningSession> sessions = topicNodeIds.isEmpty()
                ? List.of()
                : learningSessionRepository.findByUserIdAndExamScopeNodeIdIn(
                userId,
                topicNodeIds
        );

        Map<Long, LearningSession> sessionMap = sessions.stream()
                .collect(Collectors.toMap(
                        LearningSession::getExamScopeNodeId,
                        Function.identity(),
                        (first, second) -> first
                ));

        List<LearningTopicProgressResponse> topicProgresses = topicNodes.stream()
                .map(topicNode -> {
                    LearningSession session = sessionMap.get(topicNode.getId());

                    String status = session == null
                            ? STATUS_NOT_STARTED
                            : session.getStatus();

                    return new LearningTopicProgressResponse(
                            topicNode.getId(),
                            status
                    );
                })
                .toList();

        long totalTopicCount = topicNodes.size();

        long completedTopicCount = topicProgresses.stream()
                .filter(topic -> LearningSession.STATUS_COMPLETED.equals(topic.status()))
                .count();

        double progressRate = totalTopicCount == 0
                ? 0.0
                : Math.round((completedTopicCount * 1000.0 / totalTopicCount)) / 10.0;

        return new LearningProgressSummaryResponse(
                examVersionId,
                totalTopicCount,
                completedTopicCount,
                progressRate,
                topicProgresses
        );
    }
}