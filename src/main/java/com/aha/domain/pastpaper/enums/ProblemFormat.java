package com.aha.domain.pastpaper.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemFormat {
    SINGLE_CHOICE("단일 정답 객관식"),
    LONG_ANSWER("주관식");

    private final String name;
}
