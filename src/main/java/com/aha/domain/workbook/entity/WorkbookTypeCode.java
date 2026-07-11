package com.aha.domain.workbook.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkbookTypeCode {

    PAST("복원 기출"),
    TYPE2("테스트유형2"),
    TYPE3("테스트유형3");

    private final String name;
}
