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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    public PastPaperAttemptStartResponseDto getOrStartAttempt(Long paperId,
        CustomUserDetails userDetails) {

        PastPaper paper = pastPaperRepository.findByIdWithExamVersionAndExam(paperId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_NOT_FOUND));

        paper.validateCanSolve();

        Long userId = userDetails.getId();
        return findSolvingAttempt(paperId, userId)
            .map(PastPaperAttemptStartResponseDto::from)
            .orElseGet(() -> createOrGetAttempt(paper, userId));
    }


    private PastPaperAttemptStartResponseDto createOrGetAttempt(PastPaper paper, Long userId) {

        PastPaperAttempt attempt = PastPaperAttempt.create(userId, paper);

        try {
            return PastPaperAttemptStartResponseDto.from(
                pastPaperAttemptRepository.saveAndFlush(attempt));
        } catch (DataIntegrityViolationException e) {
            return findSolvingAttempt(paper.getId(), userId)
                .map(PastPaperAttemptStartResponseDto::from)
                .orElseThrow(() -> e);
        }
    }

    private Optional<PastPaperAttempt> findSolvingAttempt(Long paperId,
        Long userId) {
        return pastPaperAttemptRepository.findByPastPaper_IdAndUserIdAndStatus(paperId, userId,
            PastPaperAttemptStatus.SOLVING);
    }

    @Transactional
    public PastPaperAttemptSubmitResponseDto submitAttempt(Long attemptId,
        CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        PastPaperAttempt attempt = pastPaperAttemptRepository.findByIdWithPastPaperAndExamVersionAndExamParts(
                attemptId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));
        attempt.validateCanSubmit(userId);

        List<UserAnswer> userAnswers = userAnswerRepository.findByPastPaperAttempt_IdWithProblemAndExamScopeNode(
            attemptId);
        Set<Long> answeredProblemIds = userAnswers.stream().map(ua -> ua.getProblem().getId())
            .collect(Collectors.toSet());

        List<PastPaperItem> items = pastPaperItemRepository.findByPastPaper_IdWithProblem(
            attempt.getPastPaper().getId());
        List<Problem> problemsInPaper = items.stream().map(PastPaperItem::getProblem).toList();

        for (Problem problem : problemsInPaper) {
            if (!answeredProblemIds.contains(problem.getId())) {

                UserAnswer created = userAnswerRepository.save(
                    UserAnswer.create(attempt, problem, null, false));
                userAnswers.add(created);
                answeredProblemIds.add(problem.getId());
            }
        }

        TotalResultAggregator totalAggregator = pastPaperAttemptResultService.gradeAttempt(attempt,
            userAnswers);

        attempt.updateAfterGraded(totalAggregator);

        return PastPaperAttemptSubmitResponseDto.of(attempt, totalAggregator);
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
}
