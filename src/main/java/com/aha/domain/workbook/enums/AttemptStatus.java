package com.aha.domain.workbook.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttemptStatus {
    SOLVING("풀이 중"),
    GRADED("채점 완료");

    private final String name;
}
