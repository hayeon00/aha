package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.entity.UserAnswer;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperAttemptAnswersResponseDto(

    PastPaperAttemptStatus status,
    List<UserAnswerResponseDto> userAnswerResponses
) {

    public static PastPaperAttemptAnswersResponseDto of(PastPaperAttempt attempt, List<UserAnswer> userAnswers){
        boolean isGraded = attempt.getStatus() == PastPaperAttemptStatus.GRADED;
        List<UserAnswerResponseDto> userAnswerResponses = userAnswers.stream().map(
            ua-> UserAnswerResponseDto.from(ua,isGraded)).toList();

        return PastPaperAttemptAnswersResponseDto.builder()
            .status(attempt.getStatus())
            .userAnswerResponses(userAnswerResponses)
            .build();

    }
}
