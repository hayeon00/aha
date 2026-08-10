package com.aha.domain.pastpaper.service;

import com.aha.domain.pastpaper.dto.request.AnswerMarkedForReviewRequestDto;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final PastPaperAttemptRepository pastPaperAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ProblemRepository problemRepository;
    private final PastPaperItemRepository pastPaperItemRepository;

    public PastPaperAttemptAnswersResponseDto getAttemptsAnswer(Long attemptId,
        Long userId) {

        PastPaperAttempt attempt = pastPaperAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));

        attempt.validateOwner(userId);

        List<UserAnswer> userAnswers = userAnswerRepository.findByPastPaperAttempt_IdWithProblem(
            attemptId);

        return PastPaperAttemptAnswersResponseDto.of(attempt, userAnswers);

    }

    @Transactional
    public void saveAnswer(Long attemptId, Long problemId, AnswerSaveRequestDto requestDto,
        Long userId) {

        PastPaperAttempt existing = pastPaperAttemptRepository.findByIdForUpdate(attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));

        existing.validateCanSolve(userId);

        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        validateProblemInPastPaper(existing.getPastPaper(), problem);

        String userAnswer = requestDto.userAnswer();

        userAnswerRepository.findByPastPaperAttempt_IdAndProblem_Id(attemptId, problemId)
            .ifPresentOrElse(

                answer -> answer.updateUserAnswer(userAnswer),
                () -> userAnswerRepository.save(
                    UserAnswer.create(existing, problem, userAnswer, false)
                ));
    }

    @Transactional
    public void markForReview(
        Long attemptId, Long problemId, AnswerMarkedForReviewRequestDto requestDto, Long userId
    ) {

        PastPaperAttempt existing = pastPaperAttemptRepository.findByIdForUpdate(attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));

        existing.validateCanSolve(userId);

        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        validateProblemInPastPaper(existing.getPastPaper(), problem);

        boolean marked = requestDto.markedForReview();

        userAnswerRepository.findByPastPaperAttempt_IdAndProblem_Id(attemptId, problemId)
            .ifPresentOrElse(

                answer -> answer.updateMarkedForReview(marked),
                () -> userAnswerRepository.save(
                    UserAnswer.create(existing, problem, null, marked)
                )
            );
    }

    private void validateProblemInPastPaper(PastPaper paper, Problem problem) {
        boolean isIncluded = pastPaperItemRepository.existsByPastPaper_IdAndProblem_Id(
            paper.getId(), problem.getId());
        if (!isIncluded) {
            throw new BusinessException(ErrorCode.PROBLEM_NOT_IN_PAST_PAPER);
        }
    }
}
