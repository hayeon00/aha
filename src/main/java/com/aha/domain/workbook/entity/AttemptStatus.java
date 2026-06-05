package com.aha.domain.workbook.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttemptStatus {
    BEFORE_START("시작 전"),
    IN_PROGRESS("풀이 중"),
    SUBMITTED("제출 완료"),
    GRADED("채점 완료");

    private final String name;
}
