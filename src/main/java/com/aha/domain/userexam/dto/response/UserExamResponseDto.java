package com.aha.domain.userexam.dto.response;

import com.aha.domain.userexam.entity.UserExam;

import java.time.LocalDateTime;

public record UserExamResponseDto(
        Long userExamId,
        Long examVersionId,
        Long examId,
        String examCode,
        String examName,
        Integer versionNo,
        String versionName,
        Boolean hidden,
        LocalDateTime lastStudiedAt
) {

    public static UserExamResponseDto from(UserExam userExam) {
        return new UserExamResponseDto(
                userExam.getId(),
                userExam.getExamVersion().getId(),
                userExam.getExamVersion().getExam().getId(),
                userExam.getExamVersion().getExam().getCode(),
                userExam.getExamVersion().getExam().getName(),
                userExam.getExamVersion().getVersionNo(),
                userExam.getExamVersion().getVersionName(),
                userExam.getIsHidden(),
                userExam.getLastStudiedAt()
        );
    }
}
