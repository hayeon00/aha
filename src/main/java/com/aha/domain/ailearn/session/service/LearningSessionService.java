package com.aha.domain.ailearn.session.service;


import com.aha.domain.ailearn.content.entity.LearningContent;
import com.aha.domain.ailearn.content.repository.LearningContentRepository;
import com.aha.domain.ailearn.session.dto.request.LearningSessionCreateRequest;
import com.aha.domain.ailearn.session.dto.response.LearningSessionCreateResponse;
import com.aha.domain.ailearn.session.dto.response.LearningSessionCreateResponse;
import com.aha.domain.ailearn.session.entity.LearningSession;
import com.aha.domain.ailearn.session.repository.LearningSessionRepository;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningSessionService {

    private final LearningSessionRepository learningSessionRepository;
    private final LearningContentRepository learningContentRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;

    @Transactional
    public LearningSessionCreateResponse createSession(
            Long userId,
            LearningSessionCreateRequest request
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다.");
        }

        examScopeNodeRepository.findById(request.examScopeNodeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목차입니다."));

        Long learningContentId = request.learningContentId();

        if (learningContentId == null) {
            LearningContent content = learningContentRepository
                    .findFirstByExamScopeNodeIdAndIsActiveTrueOrderByDisplayOrderAsc(
                            request.examScopeNodeId()
                    )
                    .orElseThrow(() -> new IllegalArgumentException("해당 목차의 학습 콘텐츠가 없습니다."));

            learningContentId = content.getId();
        } else {
            learningContentRepository.findById(learningContentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학습 콘텐츠입니다."));
        }

        final Long finalLearningContentId = learningContentId;

        LearningSession session = learningSessionRepository
                .findByUserIdAndExamScopeNodeId(userId, request.examScopeNodeId())
                .map(existingSession -> {
                    existingSession.touch();
                    return existingSession;
                })
                .orElseGet(() -> learningSessionRepository.save(
                        new LearningSession(
                                userId,
                                request.examScopeNodeId(),
                                finalLearningContentId
                        )
                ));

        return LearningSessionCreateResponse.from(session);
    }
}