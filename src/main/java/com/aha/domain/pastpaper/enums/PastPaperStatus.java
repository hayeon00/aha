package com.aha.domain.pastpaper.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PastPaperStatus {

    DRAFT("초안"),
    PUBLISHED("노출 가능"),
    UNPUBLISHED("노출 불가");

    private final String name;
}
