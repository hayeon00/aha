package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.pastpaper.entity.UserAnswer;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserAnswerResponseDto(

    long problemId,
    String userAnswer,
    Boolean correct,
    boolean checked
) {

    public static UserAnswerResponseDto from(UserAnswer userAnswer, boolean isGraded) {
        return UserAnswerResponseDto.builder()
            .problemId(userAnswer.getProblem().getId())
            .userAnswer(userAnswer.getUserAnswer())
            .correct(isGraded ? userAnswer.getCorrect() : null)
            .checked(userAnswer.isMarkedForReview())
            .build();
    }

}
