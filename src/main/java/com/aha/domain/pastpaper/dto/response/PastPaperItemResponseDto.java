package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.pastpaper.entity.PastPaperItem;
import com.aha.domain.pastpaper.entity.Problem;
import com.aha.domain.pastpaper.entity.ProblemChoice;
import com.aha.domain.pastpaper.enums.ProblemFormat;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperItemResponseDto(

    int sortOrder,
    long problemId,
    String examPartName,
    String examScopeTitle,
    ProblemFormat problemFormat,
    String content,
    int score,
    String answer,
    String explanation,
    List<ProblemChoiceResponseDto> problemChoiceResponses
) {

    public static PastPaperItemResponseDto of(PastPaperItem item, boolean isGraded){

        Problem problem = item.getProblem();
        List<ProblemChoice> choices = problem.getProblemChoices();
        ExamScopeNode node = problem.getExamScopeNode();
        ExamPart part = node.getExamPart();
        List<ProblemChoiceResponseDto> problemChoiceResponses = choices.stream().map(
            ProblemChoiceResponseDto::from).toList();

        return PastPaperItemResponseDto.builder()
            .sortOrder(item.getSortOrder())
            .problemId(problem.getId())
            .examPartName(part.getName())
            .examScopeTitle(isGraded ? node.getTitle() : null)
            .problemFormat(problem.getFormat())
            .content(problem.getContent())
            .score(problem.getScore())
            .answer(isGraded ? problem.getAnswer() : null)
            .explanation(isGraded ? problem.getExplanation() : null)
            .problemChoiceResponses(problemChoiceResponses)
            .build();
    }
}
