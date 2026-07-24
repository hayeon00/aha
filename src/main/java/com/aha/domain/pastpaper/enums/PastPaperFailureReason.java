package com.aha.domain.pastpaper.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PastPaperFailureReason {

    TOTAL_SCORE_BELOW_MINIMUM("총점 미달"),
    SUBJECT_SCORE_BELOW_MINIMUM("과락");

    private final String name;
}
