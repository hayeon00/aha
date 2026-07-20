package com.aha.domain.workbook.service;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.domain.exam.repository.ExamPartRepository;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.workbook.aggregation.AttemptPartResult;
import com.aha.domain.workbook.aggregation.AttemptSectionResult;
import com.aha.domain.workbook.aggregation.SectionAggregator;
import com.aha.domain.workbook.dto.request.UserAnswerRequestDto;
import com.aha.domain.workbook.dto.response.AttemptResultResponseDto;
import com.aha.domain.workbook.dto.response.AttemptSubmitResponseDto;
import com.aha.domain.workbook.dto.response.UserAnswerResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookItemResponseDto;
import com.aha.domain.workbook.entity.Problem;
import com.aha.domain.workbook.entity.ProblemChoice;
import com.aha.domain.workbook.entity.UserAnswer;
import com.aha.domain.workbook.entity.Workbook;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import com.aha.domain.workbook.entity.WorkbookType;
import com.aha.domain.workbook.enums.AttemptStatus;
import com.aha.domain.workbook.enums.WorkbookTypeCode;
import com.aha.domain.workbook.repository.PastExamWorkbookRepository;
import com.aha.domain.workbook.repository.ProblemRepository;
import com.aha.domain.workbook.repository.UserAnswerRepository;
import com.aha.domain.workbook.repository.WorkbookAttemptRepository;
import com.aha.domain.workbook.repository.WorkbookItemRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.CustomUserDetails;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkbookAttemptService {

    private final WorkbookAttemptRepository workbookAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ProblemRepository problemRepository;
    private final WorkbookItemRepository workbookItemRepository;
    private final PastExamWorkbookRepository pastExamWorkbookRepository;
    private final ExamPartRepository examPartRepository;
    private final ExamScopeNodeRepository examScopeNodeRepository;

    @Transactional(readOnly = true)
    public List<WorkbookItemResponseDto> getItems(Long attemptId,
        CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdAndUserIdWithWorkbookAndWorkbookTypeAndExamVersionAndExam(
                attemptId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));
        if(workbookAttempt.isSolving()){
            Workbook workbook = workbookAttempt.getWorkbook();
            workbook.validateGetItems();
            return workbookItemRepository.findByWorkbook_IdWithProblemAndExamScopeNodeAndExamPartAndProblemChoices(workbook.getId()).stream()
                .map(wi -> {
                    Problem problem = wi.getProblem();
                    List<ProblemChoice> problemChoices = problem.getProblemChoices();
                    ExamScopeNode examScopeNode = problem.getExamScopeNode();
                    ExamPart examPart = examScopeNode.getExamPart();
                    return WorkbookItemResponseDto.ofSolving(wi, problem, problemChoices, examPart);
                }).toList();
        }else{
            //워크북 검증 x
            //추후 작성 예정
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<UserAnswerResponseDto> getUserAnswers(Long attemptId,
        CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdAndUserIdWithWorkbookAndWorkbookTypeAndExamVersionAndExam(
                attemptId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));
        workbookAttempt.getWorkbook().validateGetUserAnswers();

        List<UserAnswer> userAnswers = userAnswerRepository.findByWorkbookAttempt_Id(attemptId);

        return userAnswers.stream().map(UserAnswerResponseDto::fromSolving).toList();
        //GRADED는 다음 이슈에서

    }

    @Transactional
    public void saveUserAnswer(Long attemptId, Long problemId, CustomUserDetails userDetails,
        UserAnswerRequestDto requestDto) {
        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdAndUserIdWithWorkbookAndWorkbookTypeAndExamVersionAndExam(
                attemptId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));
        Workbook workbook = workbookAttempt.getWorkbook();
        workbook.validateSaveUserAnswers();

        WorkbookType workbookType = workbook.getWorkbookType();
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
        boolean isIncluded = workbookItemRepository.existsByProblem_IdAndWorkbook_Id(problemId,
            workbook.getId());
        if (!isIncluded) {
            throw new BusinessException(ErrorCode.WORKBOOK_NOT_INCLUDE_PROBLEM);
        }
        Optional<UserAnswer> mayBeUserAnswer = userAnswerRepository.findByProblem_IdAndWorkbookAttempt_Id(
            problemId, attemptId);
        String userAnswer = requestDto.userAnswer();

        if (workbookType.getCode() == WorkbookTypeCode.PAST) {
            pastExamWorkbookRepository.findById(workbook.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKBOOK_NOT_MATCH_PAST));
            workbookAttempt.validateSaveUserAnswer(workbook);
            if (mayBeUserAnswer.isEmpty()) {
                userAnswerRepository.save(
                    UserAnswer.create(workbookAttempt, problem, userAnswer, false));
            } else {
                mayBeUserAnswer.get().update(userAnswer);
            }
        } else {
            throw new BusinessException(ErrorCode.WORKBOOK_TYPE_NOT_SUPPORTED);
        }
    }

    @Transactional
    public void checkUserAnswer(Long attemptId, Long problemId, CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdAndUserIdWithWorkbookAndWorkbookTypeAndExamVersionAndExam(
                attemptId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));
        Workbook workbook = workbookAttempt.getWorkbook();
        workbook.validateSaveUserAnswers();

        WorkbookType workbookType = workbook.getWorkbookType();
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
        boolean isIncluded = workbookItemRepository.existsByProblem_IdAndWorkbook_Id(problemId,
            workbook.getId());
        if (!isIncluded) {
            throw new BusinessException(ErrorCode.WORKBOOK_NOT_INCLUDE_PROBLEM);
        }
        Optional<UserAnswer> mayBeUserAnswer = userAnswerRepository.findByProblem_IdAndWorkbookAttempt_Id(
            problemId, attemptId);

        if (workbookType.getCode() == WorkbookTypeCode.PAST) {
            pastExamWorkbookRepository.findById(workbook.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKBOOK_NOT_MATCH_PAST));
            workbookAttempt.validateSaveUserAnswer(workbook);
            if (mayBeUserAnswer.isEmpty()) {
                userAnswerRepository.save(UserAnswer.create(workbookAttempt, problem, null, true));
            } else {
                mayBeUserAnswer.get().update();
            }
        } else {
            throw new BusinessException(ErrorCode.WORKBOOK_TYPE_NOT_SUPPORTED);
        }
    }

    @Transactional
    public AttemptSubmitResponseDto submitAttempt(CustomUserDetails userDetails, Long attemptId) {
        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdAndUserIdWithWorkbookAndWorkbookTypeAndExamVersionAndExam(
                attemptId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));

        Workbook workbook = workbookAttempt.getWorkbook();
        workbook.validateSubmitAttempt();

        int userAnswerCount = userAnswerRepository.countByWorkbookAttempt_Id(attemptId);

        workbookAttempt.validateSubmitAttempt(userAnswerCount);


        List<UserAnswer> userAnswers = userAnswerRepository.findByAttempt_IdWithProblemAndExamScopeNode(
            attemptId);
        for (UserAnswer userAnswer : userAnswers) {
            Problem problem = userAnswer.getProblem();
            problem.grade(userAnswer);
        }

        List<ExamPart> examParts = examPartRepository.findByExamVersion_IdWithExamScopeNodes(
            workbook.getExamVersion().getId());
        Map<Long, ExamPart> partMap = examParts.stream()
            .collect(Collectors.toMap(ExamPart::getId, ep -> ep));

        Map<Long, Map<Long, SectionAggregator>> aggregatorMap = new HashMap<>();
        for (ExamPart examPart : examParts) {
            List<ExamScopeNode> sectionNodes = examPart.getExamScopeNodes().stream()
                .filter(esn -> esn.getNodeType() == ExamScopeNodeType.SECTION)
                .toList();
            HashMap<Long, SectionAggregator> map = new HashMap<>();
            for (ExamScopeNode examScopeNode : sectionNodes) {
                map.put(examScopeNode.getId(), SectionAggregator.create(examScopeNode));
            }
            aggregatorMap.put(examPart.getId(), map);
        }

        for (UserAnswer userAnswer : userAnswers) {
            Problem problem = userAnswer.getProblem();
            ExamScopeNode examScopeNode = problem.getExamScopeNode();
            ExamScopeNode sectionNode = examScopeNode.findSectionNode();
            SectionAggregator aggregator = aggregatorMap.get(sectionNode.getExamPart().getId())
                .get(sectionNode.getId());
            int problemScore = problem.getScore();
            if (userAnswer.isCorrect()) {
                aggregator.increaseUserScore(problemScore);
                aggregator.increaseCorrectQuestionCount();
            }
            aggregator.increaseTotalQuestionCount();
            aggregator.increaseTotalScore(problemScore);
        }
        boolean isPassed = true;
        String failedSubject = "";
        String resultMessage = "";
        int totalUserScore = 0;
        List<AttemptPartResult> partResults = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, SectionAggregator>> entry : aggregatorMap.entrySet()) {
            Long partId = entry.getKey();
            ExamPart examPart = partMap.get(partId);
            List<SectionAggregator> aggregators = new ArrayList<>(entry.getValue().values());
            int partUserScore = 0;
            partUserScore += aggregators.stream().mapToInt(SectionAggregator::getUserScore).sum();
            totalUserScore += partUserScore;
            List<AttemptSectionResult> sectionResults = aggregators.stream().map(
                    AttemptSectionResult::create)
                .sorted(Comparator.comparing(AttemptSectionResult::displayOrder)).toList();
            if (examPart.isSubjectFailTarget()) {
                int threshold = examPart.getSubjectFailThresholdScore();
                if (threshold > partUserScore) {
                    isPassed = false;
                    failedSubject += examPart.getName() + " ";
                }
            }

            partResults.add(AttemptPartResult.create(examPart, sectionResults));
        }

        partResults.sort(Comparator.comparing(AttemptPartResult::displayOrder));

        ExamVersion examVersion = workbook.getExamVersion();
        int passingScore = examVersion.getPassingScore();
        if (examVersion.getPassingRuleType().equals("TOTAL")) {
            if (passingScore > totalUserScore) {
                isPassed = false;
                resultMessage = "총점 미달입니다.";
            }
        } else {
            if ((double) passingScore > (double) totalUserScore / aggregatorMap.size()) {
                isPassed = false;
                resultMessage = "평균 미달입니다.";
            }
        }

        if (resultMessage.isEmpty()) {
            if (isPassed) {
                resultMessage = "축하합니다. 합격입니다!";
            } else {
                resultMessage = failedSubject + "에서 과락입니다.";
            }
        }

        workbookAttempt.updateAfterGrade(partResults, isPassed, resultMessage);

        return AttemptSubmitResponseDto.from(workbookAttempt);
    }

    @Transactional(readOnly = true)
    public AttemptResultResponseDto getResult(CustomUserDetails userDetails, Long attemptId) {

        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdAndUserIdWithWorkbookAndWorkbookTypeAndExamVersionAndExam(
                attemptId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));
        workbookAttempt.validateGetResult();

        return AttemptResultResponseDto.from(workbookAttempt);
    }
}
