package com.aha.domain.workbook.dto.response;

import java.util.Map;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkbookAttemptResponseDto {

  private WorkbookAttemptMeta meta;
  private List<WorkbookItemResponse> workbookItemResponses;

  @Getter
  @AllArgsConstructor
  public static class WorkbookAttemptMeta {

    private Long workbookAttemptId;
    private Integer totalQuestionCount;
    private Integer answerCount;
    private Integer checkedCount;
    private Integer uncertainCount;
  }

  @Getter
  @AllArgsConstructor
  public static class WorkbookItemResponse {

    private Integer itemNo;
    private Long itemId;
    private String choiceType;
    private String answerType;
    private Map<String, Object> problemContent;
    private List<ProblemChoiceResponse> problemChoices;
  }

  @Getter
  @AllArgsConstructor
  public static class ProblemChoiceResponse {

    private Integer choiceNo;
    private Long choiceId;
    private Map<String, Object> choiceContent;
  }

}