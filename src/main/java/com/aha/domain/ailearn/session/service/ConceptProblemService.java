package com.aha.domain.ailearn.session.service;

import com.aha.domain.ailearn.session.dto.response.ConceptProblemChoiceResponse;
import com.aha.domain.ailearn.session.dto.response.ConceptProblemListResponse;
import com.aha.domain.ailearn.session.dto.response.ConceptProblemResponse;
import com.aha.domain.ailearn.session.entity.LearningSession;
import com.aha.domain.ailearn.session.repository.LearningSessionRepository;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.problem.entity.Problem;
import com.aha.domain.problem.entity.ProblemChoice;
import com.aha.domain.problem.repository.ProblemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConceptProblemService {

    private final LearningSessionRepository learningSessionRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;
    private final ProblemRepository problemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConceptProblemListResponse getConceptProblems(
            Long userId,
            Long learningSessionId
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

        ExamScopeNode examScopeNode = examScopeNodeRepository.findById(examScopeNodeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목차입니다."));

        List<Problem> problems =
                problemRepository.findActiveConceptProblemsWithChoices(examScopeNodeId);

        if (problems.isEmpty()) {
            throw new IllegalArgumentException("해당 목차의 개념확인 문제가 없습니다.");
        }

        List<ConceptProblemResponse> problemResponses = problems.stream()
                .map(this::toProblemResponse)
                .toList();

        return new ConceptProblemListResponse(
                session.getId(),
                examScopeNode.getId(),
                examScopeNode.getTitle(),
                problemResponses.size(),
                problemResponses
        );
    }

    private ConceptProblemResponse toProblemResponse(Problem problem) {
        String questionText = extractJsonText(
                problem.getQuestionContentJson(),
                "questionText"
        );

        List<ConceptProblemChoiceResponse> choices = problem.getChoices().stream()
                .sorted(Comparator.comparing(ProblemChoice::getChoiceNo))
                .map(choice -> new ConceptProblemChoiceResponse(
                        choice.getId(),
                        choice.getChoiceNo(),
                        extractJsonText(choice.getChoiceContentJson(), "choiceText")
                ))
                .toList();

        return new ConceptProblemResponse(
                problem.getId(),
                questionText,
                choices
        );
    }

    private String extractJsonText(String json, String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode valueNode = node.get(fieldName);

            if (valueNode == null || valueNode.isNull()) {
                return "";
            }

            return valueNode.asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 파싱 중 오류가 발생했습니다.");
        }
    }
}