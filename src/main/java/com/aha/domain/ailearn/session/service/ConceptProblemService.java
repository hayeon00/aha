package com.aha.domain.ailearn.session.service;

import com.aha.domain.ailearn.session.dto.request.ConceptProblemAnswerRequest;
import com.aha.domain.ailearn.session.dto.request.ConceptProblemSubmitRequest;
import com.aha.domain.ailearn.session.dto.response.*;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Transactional
    public ConceptProblemSubmitResponse submitConceptProblems(
            Long userId,
            Long learningSessionId,
            ConceptProblemSubmitRequest request
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

        List<Long> submittedProblemIds = request.answers().stream()
                .map(ConceptProblemAnswerRequest::problemId)
                .toList();

        Set<Long> distinctProblemIds = Set.copyOf(submittedProblemIds);

        if (submittedProblemIds.size() != distinctProblemIds.size()) {
            throw new IllegalArgumentException("중복 제출된 문제가 있습니다.");
        }

        List<Problem> problems = problemRepository.findActiveConceptProblemsByIds(
                examScopeNodeId,
                submittedProblemIds
        );

        if (problems.size() != submittedProblemIds.size()) {
            throw new IllegalArgumentException("현재 학습 세션에 속하지 않는 문제가 포함되어 있습니다.");
        }

        Map<Long, Problem> problemMap = problems.stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity()));

        List<ConceptProblemResultResponse> results = request.answers().stream()
                .map(answer -> {
                    Problem problem = problemMap.get(answer.problemId());

                    Integer correctChoiceNo = extractJsonInt(
                            problem.getAnswerJson(),
                            "correctChoiceNo"
                    );

                    String questionText = extractJsonText(
                            problem.getQuestionContentJson(),
                            "questionText"
                    );

                    String explanationText = extractJsonText(
                            problem.getExplanationJson(),
                            "explanationText"
                    );

                    boolean correct = correctChoiceNo.equals(answer.selectedChoiceNo());

                    return new ConceptProblemResultResponse(
                            problem.getId(),
                            questionText,
                            answer.selectedChoiceNo(),
                            correctChoiceNo,
                            correct,
                            explanationText
                    );
                })
                .toList();

        int totalCount = results.size();
        int correctCount = (int) results.stream()
                .filter(ConceptProblemResultResponse::correct)
                .count();

        int wrongCount = totalCount - correctCount;
        double correctRate = totalCount == 0
                ? 0.0
                : Math.round((correctCount * 1000.0 / totalCount)) / 10.0;

        session.complete();

        return new ConceptProblemSubmitResponse(
                session.getId(),
                examScopeNode.getId(),
                examScopeNode.getTitle(),
                totalCount,
                correctCount,
                wrongCount,
                correctRate,
                results
        );
    }

    private Integer extractJsonInt(String json, String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode valueNode = node.get(fieldName);

            if (valueNode == null || valueNode.isNull()) {
                throw new IllegalArgumentException("JSON 필드가 존재하지 않습니다: " + fieldName);
            }

            return valueNode.asInt();
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 파싱 중 오류가 발생했습니다.");
        }
    }

}