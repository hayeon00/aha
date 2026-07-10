package com.aha.domain.workbook.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkbookStatus {
    DRAFT("초안"),
    PUBLISHED("노출 가능"),
    UNPUBLISHED("노출 불가"),
    ARCHIVED("보관");

    private final String name;
}
