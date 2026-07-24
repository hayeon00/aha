package com.aha.domain.pastpaper.service;

import com.aha.domain.pastpaper.dto.response.PastPaperItemResponseDto;
import com.aha.domain.pastpaper.entity.PastPaper;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import com.aha.domain.pastpaper.repository.PastPaperAttemptRepository;
import com.aha.domain.pastpaper.repository.PastPaperItemRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PastPaperItemService {

    private final PastPaperItemRepository pastPaperItemRepository;
    private final PastPaperAttemptRepository pastPaperAttemptRepository;

    @Transactional(readOnly = true)
    public List<PastPaperItemResponseDto> getItems(Long attemptId, CustomUserDetails userDetails) {

        PastPaperAttempt attempt = pastPaperAttemptRepository.findById(attemptId)
            .orElseThrow(()->new BusinessException(ErrorCode.PAST_PAPER_ATTEMPT_NOT_FOUND));
        Long userId = userDetails.getId();
        attempt.validateOwner(userId);

        PastPaper paper = attempt.getPastPaper();

        boolean isGraded = isGraded(attempt);

        return pastPaperItemRepository.findByPastPaper_IdWithProblemAndExamScopeNodeAndExamPart(paper.getId())
            .stream()
            .map(ppi->PastPaperItemResponseDto.of(ppi,isGraded))
            .toList();
    }

    private boolean isGraded(PastPaperAttempt attempt) {
        return attempt.getStatus() == PastPaperAttemptStatus.GRADED;
    }
}
