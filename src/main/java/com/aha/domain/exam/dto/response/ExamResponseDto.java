package com.aha.domain.exam.dto.response;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamVersion;

public record ExamResponseDto(
        Long id,
        String code,
        String name,
        String status,
        Long activeVersionId,
        String versionName
) {

    public static ExamResponseDto from(Exam exam, ExamVersion activeVersion) {
        return new ExamResponseDto(
                exam.getId(),
                exam.getCode(),
                exam.getName(),
                exam.getStatus().name(),
                activeVersion != null ? activeVersion.getId() : null,
                activeVersion != null ? activeVersion.getVersionName() : null
        );
    }
}