package com.aha.domain.workbook.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FailReasonCode {
    TOTAL_SCORE_UNDER("총점 미달"),
    SUBJECT_FAIL("과락"),
    AVERAGE_SCORE_UNDER("평균 미달");

    private final String description;
}
