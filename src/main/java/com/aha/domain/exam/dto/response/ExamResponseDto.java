package com.aha.domain.exam.dto.response;

import com.aha.domain.exam.entity.Exam;
import lombok.Getter;

@Getter
public class ExamResponseDto {

    private final Long id;
    private final String code;
    private final String name;

    public ExamResponseDto(Exam exam) {
        this.id = exam.getId();
        this.code = exam.getCode();
        this.name = exam.getName();
    }
}