package com.aha.domain.workbook.service;

import com.aha.domain.workbook.dto.request.UserAnswerRequestDto;
import com.aha.domain.workbook.dto.response.UserAnswerResponseDto;
import com.aha.domain.workbook.entity.Problem;
import com.aha.domain.workbook.entity.UserAnswer;
import com.aha.domain.workbook.entity.Workbook;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import com.aha.domain.workbook.entity.WorkbookType;
import com.aha.domain.workbook.enums.WorkbookTypeCode;
import com.aha.domain.workbook.repository.PastExamWorkbookRepository;
import com.aha.domain.workbook.repository.ProblemRepository;
import com.aha.domain.workbook.repository.UserAnswerRepository;
import com.aha.domain.workbook.repository.WorkbookAttemptRepository;
import com.aha.domain.workbook.repository.WorkbookItemRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.CustomUserDetails;
import java.util.List;
import java.util.Optional;
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

    @Transactional(readOnly = true)
    public List<UserAnswerResponseDto> getUserAnswers(Long attemptId,
        CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdWithWorkbookAndWorkbookTypeExamVersionAndExam(
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
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdWithWorkbookAndWorkbookTypeExamVersionAndExam(
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
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByIdWithWorkbookAndWorkbookTypeExamVersionAndExam(
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


}
