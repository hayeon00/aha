package com.aha.domain.workbook.dto.response;

import com.aha.domain.workbook.entity.UserAnswer;
import lombok.Builder;

@Builder
public record UserAnswerResponseDto(

    Long problemId,
    String userAnswer,
    Boolean correct,
    Boolean checked
) {
    public static UserAnswerResponseDto fromSolving(UserAnswer userAnswer){
        return UserAnswerResponseDto.builder()
            .problemId(userAnswer.getProblem().getId())
            .userAnswer(userAnswer.getAnswer())
            .correct(null)
            .checked(userAnswer.isChecked())
            .build();
    }
}
/****
 * {
 * 	"success":true,
 * 	"status":200,
 * 	"message":"답안 조회 성공", // GRADED 시 , 정답 여부 포함 답안 조회 성공
 * 	"data":[
 *
 * 		        {
 * 			"problemId":5,
 * 			"userAnswer": "1",
 * 			"correct":null,
 * 			"checked":true
 *        }, ...
 *
 * 	]
 * }
 */