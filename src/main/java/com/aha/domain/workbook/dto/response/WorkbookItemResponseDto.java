package com.aha.domain.workbook.dto.response;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.enums.ExamPartCode;
import com.aha.domain.exam.enums.ExamScopeNodeCode;
import com.aha.domain.workbook.entity.Problem;
import com.aha.domain.workbook.entity.ProblemChoice;
import com.aha.domain.workbook.enums.ProblemFormat;
import com.aha.domain.workbook.entity.WorkbookItem;
import java.util.List;
import lombok.Builder;

@Builder
public record WorkbookItemResponseDto(
    Integer sortOrder,
    Long problemId,
    ExamPartCode examPartCode,
    String examPartCodeName,
    ExamScopeNodeCode examScopeNodeCode,
    String examScopeNodeCodeTitle,
    ProblemFormat problemFormat,
    String content,
    Integer score,
    String answer,
    String explanation,
    List<ProblemChoiceResponse> choices

) {

    @Builder
    public record ProblemChoiceResponse(
        String content,
        Integer sortOrder
    ){
        public static ProblemChoiceResponse from(ProblemChoice problemChoice){
            return ProblemChoiceResponse.builder()
                .content(problemChoice.getContent())
                .sortOrder(problemChoice.getSortOrder())
                .build();
        }
    }

    public static WorkbookItemResponseDto ofSolving(WorkbookItem workbookItem, Problem problem,List<ProblemChoice> problemChoices,ExamPart examPart) {
        List<ProblemChoiceResponse> choices = problemChoices
            .stream().map(ProblemChoiceResponse::from).toList();
        return WorkbookItemResponseDto.builder()
            .sortOrder(workbookItem.getSortOrder())
            .problemId(problem.getId())
            .examPartCode(examPart.getCode())
            .examPartCodeName(examPart.getName())
            .examScopeNodeCode(null)
            .examScopeNodeCodeTitle(null)
            .problemFormat(problem.getFormat())
            .content(problem.getContent())
            .score(problem.getScore())
            .answer(null)
            .explanation(null)
            .choices(choices)
            .build();
    }


}
