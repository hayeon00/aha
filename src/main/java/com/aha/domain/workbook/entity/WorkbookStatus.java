package com.aha.domain.workbook.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum WorkbookStatus {
  ARCHIVED("보관"),
  REVIEWED("검수 완료");

  private final String name;
}
