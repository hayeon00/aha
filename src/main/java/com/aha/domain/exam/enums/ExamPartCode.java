package com.aha.domain.exam.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamPartCode {
    SUBJECT_1("1과목"),
    SUBJECT_2("2과목");

    private final String name;
}
