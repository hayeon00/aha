package com.aha.domain.ailearn.assistant.service;

import com.aha.domain.ailearn.assistant.dto.request.AssistantMessageRequest;
import com.aha.domain.ailearn.assistant.dto.response.AssistantMessageResponse;
import com.aha.domain.ailearn.assistant.entity.AiMessage;
import com.aha.domain.ailearn.assistant.entity.AiQuestionType;
import com.aha.domain.ailearn.assistant.repository.AiMessageRepository;
import com.aha.domain.ailearn.session.entity.LearningSession;
import com.aha.domain.ailearn.session.repository.LearningSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningAssistantService {

    private final LearningSessionRepository learningSessionRepository;
    private final AiMessageRepository aiMessageRepository;

    @Transactional
    public AssistantMessageResponse createAssistantMessage(
            Long userId,
            Long learningSessionId,
            AssistantMessageRequest request
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다.");
        }

        LearningSession session = learningSessionRepository.findById(learningSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학습 세션입니다."));

        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 학습 세션에 접근할 수 없습니다.");
        }

        Long examScopeNodeId = session.getExamScopeNodeId();

        AiMessage userMessage = AiMessage.userMessage(
                session.getId(),
                userId,
                examScopeNodeId,
                request.questionType(),
                request.message()
        );

        aiMessageRepository.save(userMessage);

        String assistantText = generateTemporaryAnswer(
                request.questionType(),
                request.message()
        );

        AiMessage assistantMessage = AiMessage.assistantMessage(
                session.getId(),
                userId,
                examScopeNodeId,
                request.questionType(),
                assistantText
        );

        aiMessageRepository.save(assistantMessage);

        session.touch();

        return new AssistantMessageResponse(
                session.getId(),
                examScopeNodeId,
                request.questionType(),
                request.message(),
                assistantText
        );
    }

    private String generateTemporaryAnswer(
            AiQuestionType questionType,
            String message
    ) {
        return switch (questionType) {
            case EASY_EXPLANATION ->
                    "쉽게 말하면, 현재 학습 중인 개념을 시험에서 이해하기 쉽게 풀어 설명하는 답변입니다. 질문: " + message;

            case COMPARISON ->
                    "비교할 때는 정의, 목적, 사용 상황, 시험에서 헷갈리는 포인트를 나누어 보는 것이 좋습니다. 질문: " + message;

            case EXAM_POINT ->
                    "시험에서는 이 개념의 정의, 키워드, 헷갈리는 표현이 주로 출제될 수 있습니다. 질문: " + message;

            case PROBLEM_HELP ->
                    "문제를 풀 때는 먼저 핵심 키워드를 찾고, 보기에서 틀린 조건을 제거하는 방식으로 접근해보세요. 질문: " + message;

            case SUMMARY ->
                    "현재 개념의 핵심은 정의를 이해하고, 시험에서 어떤 표현으로 나오는지 확인하는 것입니다. 질문: " + message;

            case FREE_QNA ->
                    "현재 학습 중인 개념 범위 안에서 질문에 답변합니다. 질문: " + message;
        };
    }
}