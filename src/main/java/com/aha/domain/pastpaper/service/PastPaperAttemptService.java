package com.aha.domain.pastpaper.service;

import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import com.aha.domain.pastpaper.dto.response.PastPaperAttemptStartResponseDto;
import com.aha.domain.pastpaper.dto.response.result.PastPaperAttemptResponseDto;
import com.aha.domain.pastpaper.dto.response.result.PastPaperAttemptResultResponseDto;
import com.aha.domain.pastpaper.dto.response.result.PastPaperAttemptSubmitResponseDto;
import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.entity.PastPaperItem;
import com.aha.domain.pastpaper.entity.Problem;
import com.aha.domain.pastpaper.entity.UserAnswer;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import com.aha.domain.pastpaper.repository.PastPaperAttemptRepository;
import com.aha.domain.pastpaper.repository.PastPaperItemRepository;
import com.aha.domain.pastpaper.repository.PastPaperRepository;
import com.aha.domain.pastpaper.repository.UserAnswerRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.response.PageResponseDto;
import com.aha.global.security.CustomUserDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PastPaperAttemptService {

    private final PastPaperRepository pastPaperRepository;
    private final PastPaperAttemptRepository pastPaperAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final PastPaperItemRepository pastPaperItemRepository;
    private final PastPaperAttemptResultService pastPaperAttemptResultService;

    @Transactional
    public PastPaperAttemptStartResponseDto getOrStartAttempt(Long paperId,
        Long userId) {

        PastPaper paper = pastPaperRepository.findByIdWithExamVersionAndExam(paperId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_NOT_FOUND));

        paper.validateCanSolve();

        int timeLimit = paper.getExamVersion().getDefaultDurationSeconds();

        pastPaperAttemptRepository.insertOrReuseSolvingAttempt(userId,paper.getId(),timeLimit);

        long attemptId = pastPaperAttemptRepository.findLastInsertId();

        return PastPaperAttemptStartResponseDto.from(
            pastPaperAttemptRepository.findById(attemptId)
                .orElseThrow(()->new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND))
        );
    }

    @Transactional(readOnly = true)
    public PageResponseDto<PastPaperAttemptResponseDto> getAttempts(
        CustomUserDetails userDetails,
        Pageable pageable,
        PastPaperAttemptStatus status
    ) {

        Page<PastPaperAttemptResponseDto> result = pastPaperAttemptRepository
            .findAllByUserIdAndStatus(userDetails.getId(), status, pageable)
            .map(PastPaperAttemptResponseDto::from);

        return PageResponseDto.from(result);
    }

    @Transactional(readOnly = true)
    public PastPaperAttemptResultResponseDto getResult(CustomUserDetails userDetails, Long attemptId) {

        PastPaperAttempt attempt = pastPaperAttemptRepository.findByIdWithPastPaperAndExamVersionAndExamParts(
                attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));

        attempt.validateCanSeeResult(userDetails.getId());

        List<UserAnswer> userAnswers = userAnswerRepository.findByPastPaperAttempt_IdWithProblemAndExamScopeNode(attemptId);

        TotalResultAggregator totalAggregator = pastPaperAttemptResultService.calculateResult(attempt, userAnswers);

        return PastPaperAttemptResultResponseDto.from(totalAggregator);
    }

    @Transactional
    public void submitExpiredAttempt(Long attemptId) {
        PastPaperAttempt attempt = pastPaperAttemptRepository
            .findByIdForUpdate(attemptId)
            .orElse(null);

        if (attempt == null) {
            return;
        }

        LocalDateTime completedAt = LocalDateTime.now();

        if (!attempt.isExpiredSolving(completedAt)) {
            return;
        }

        completeAttempt(attempt, completedAt);
    }

    @Transactional
    public PastPaperAttemptSubmitResponseDto submitAttempt(
        Long attemptId,
        Long userId
    ) {
        PastPaperAttempt attempt = pastPaperAttemptRepository
            .findByIdForUpdate(attemptId)
            .orElseThrow(() ->
                new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND)
            );

        attempt.validateCanSubmit(userId);

        TotalResultAggregator result =
            completeAttempt(attempt, LocalDateTime.now());

        return PastPaperAttemptSubmitResponseDto.of(attempt, result);
    }

    private TotalResultAggregator completeAttempt(
        PastPaperAttempt attempt,
        LocalDateTime completedAt
    ) {
        List<UserAnswer> answers = new ArrayList<>(
            userAnswerRepository
                .findByPastPaperAttempt_IdWithProblemAndExamScopeNode(
                    attempt.getId()
                )
        );

        List<PastPaperItem> items = pastPaperItemRepository
            .findByPastPaper_IdWithProblem(
                attempt.getPastPaper().getId()
            );

        addMissingAnswers(attempt, answers, items);

        TotalResultAggregator result =
            pastPaperAttemptResultService.gradeAttempt(attempt, answers);

        attempt.updateAfterGraded(result, completedAt);

        return result;
    }

    private void addMissingAnswers(
        PastPaperAttempt attempt,
        List<UserAnswer> answers,
        List<PastPaperItem> items
    ) {
        Set<Long> answeredProblemIds =
            answers.stream()
                .map(answer ->
                    answer.getProblem().getId()
                )
                .collect(Collectors.toSet());

        for (PastPaperItem item : items) {
            Problem problem = item.getProblem();

            if (answeredProblemIds.contains(problem.getId())) {
                continue;
            }

            UserAnswer missingAnswer =
                UserAnswer.create(
                    attempt,
                    problem,
                    null,
                    false
                );

            userAnswerRepository.save(missingAnswer);
            answers.add(missingAnswer);
            answeredProblemIds.add(problem.getId());
        }
    }
}
