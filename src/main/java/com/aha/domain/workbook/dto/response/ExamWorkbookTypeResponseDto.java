package com.aha.domain.workbook.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExamWorkbookTypeResponseDto {

  private String workbookTypeCode;
  private String workbookTypeName;
  private Integer displayOrder;

}
