package com.aha.domain.workbook.dto.response;

import com.aha.domain.workbook.enums.AttemptStatus;
import com.aha.domain.workbook.entity.PastExamWorkbook;
import com.aha.domain.workbook.entity.Workbook;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AttemptStartResponseDto(
    Long id,
    AttemptStatus status,
    LocalDateTime createdAt,
    Integer timeLimit
) {

    public static AttemptStartResponseDto of(WorkbookAttempt workbookAttempt, Workbook workbook) {
        PastExamWorkbook past = workbook.getPastExamWorkbook();
        return AttemptStartResponseDto.builder()
            .id(workbookAttempt.getId())
            .status(workbookAttempt.getStatus())
            .createdAt(workbookAttempt.getCreatedAt())
            .timeLimit(past != null ? past.getTimeLimit() : null)
            .build();
    }
}
