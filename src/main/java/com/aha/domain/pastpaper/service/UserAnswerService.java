package com.aha.domain.pastpaper.service;

import com.aha.domain.pastpaper.dto.request.AnswerSaveRequestDto;
import com.aha.domain.pastpaper.dto.response.PastPaperAttemptAnswersResponseDto;
import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.entity.Problem;
import com.aha.domain.pastpaper.entity.UserAnswer;
import com.aha.domain.pastpaper.repository.PastPaperAttemptRepository;
import com.aha.domain.pastpaper.repository.PastPaperItemRepository;
import com.aha.domain.pastpaper.repository.UserAnswerRepository;
import com.aha.domain.pastpaper.repository.ProblemRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final PastPaperAttemptRepository pastPaperAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ProblemRepository problemRepository;
    private final PastPaperItemRepository pastPaperItemRepository;

    public PastPaperAttemptAnswersResponseDto getAttemptsAnswer(Long attemptId,
        CustomUserDetails userDetails) {

        PastPaperAttempt attempt = pastPaperAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));

        Long userId = getUserId(userDetails);

        attempt.validateOwner(userId);

        List<UserAnswer> userAnswers = userAnswerRepository.findByPastPaperAttempt_IdWithProblem(
            attemptId);

        return PastPaperAttemptAnswersResponseDto.of(attempt, userAnswers);

    }

    public void saveAnswer(Long attemptId, Long problemId, AnswerSaveRequestDto request,
        CustomUserDetails userDetails) {

        Long userId = getUserId(userDetails);
        PastPaperAttempt attempt = pastPaperAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));
        attempt.validateCanSolve(userId);

        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        validateProblemInPastPaper(attempt.getPastPaper(), problem);

        String userAnswer =request.userAnswer();

        userAnswerRepository.findByPastPaperAttempt_IdAndProblem_Id(attemptId, problemId)
            .ifPresentOrElse(ua -> updateUserAnswer(userAnswer, ua),
                () -> createOrUpdateUserAnswer(attempt, problem, userAnswer));
    }

    private void createOrUpdateUserAnswer(PastPaperAttempt attempt, Problem problem,
        String userAnswer) {
        try {
            userAnswerRepository.saveAndFlush(
                UserAnswer.create(attempt, problem, userAnswer, false));
        } catch (DataIntegrityViolationException e) {
            userAnswerRepository.findByPastPaperAttempt_IdAndProblem_Id(attempt.getId(),
                problem.getId()).ifPresentOrElse(ua -> updateUserAnswer(userAnswer, ua), () -> {
                throw e;
            });
        }
    }

    private void updateUserAnswer(String userAnswer, UserAnswer ua) {
        ua.updateUserAnswer(userAnswer);
        userAnswerRepository.saveAndFlush(ua);
    }

    public void toggleMarkedForReview(Long attemptId, Long problemId,
        CustomUserDetails userDetails) {

        Long userId = getUserId(userDetails);
        PastPaperAttempt attempt = pastPaperAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));
        attempt.validateCanSolve(userId);

        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        validateProblemInPastPaper(attempt.getPastPaper(), problem);

        userAnswerRepository.findByPastPaperAttempt_IdAndProblem_Id(attemptId, problemId)
            .ifPresentOrElse(this::toggleMarked,
                () -> createMarkedAnswer(attempt, problem));
    }


    private void validateProblemInPastPaper(PastPaper paper, Problem problem) {
        boolean isIncluded = pastPaperItemRepository.existsByPastPaper_IdAndProblem_Id(
            paper.getId(), problem.getId());
        if (!isIncluded) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_IN_PAST_PAPER);
        }
    }

    private void toggleMarked(UserAnswer ua) {
        ua.toggleChecked();
        userAnswerRepository.saveAndFlush(ua);
    }

    private void createMarkedAnswer(PastPaperAttempt attempt, Problem problem) {
        try {
            userAnswerRepository.saveAndFlush(UserAnswer.create(attempt, problem, null, true));
        } catch (DataIntegrityViolationException e) {
            userAnswerRepository.findByPastPaperAttempt_IdAndProblem_Id(attempt.getId(),
                problem.getId()).ifPresentOrElse(this::toggleMarked, () -> {
                throw e;
            });
        }
    }

    private Long getUserId(CustomUserDetails userDetails) {
        return userDetails.getId();
    }
}
