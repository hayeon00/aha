package com.aha.domain.exam.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamVersionStatus {

    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private final String name;
}
