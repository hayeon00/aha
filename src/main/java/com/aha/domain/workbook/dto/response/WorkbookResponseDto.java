package com.aha.domain.workbook.dto.response;

import com.aha.domain.workbook.entity.PastExamWorkbook;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record WorkbookResponseDto(
    Long id,
    Boolean reviewed,
    Integer year,
    Integer roundNo,
    Integer timeLimit,
    Integer totalProblemCount,
    LocalDateTime examDate
) {

    public static WorkbookResponseDto from(PastExamWorkbook pew) {
        return WorkbookResponseDto.builder()
            .id(pew.getWorkbookId())
            .reviewed(pew.getIsReviewed())
            .year(pew.getYear())
            .roundNo(pew.getRoundNo())
            .timeLimit(pew.getTimeLimit())
            .totalProblemCount(pew.getTotalProblemCount())
            .examDate(pew.getExamDate())
            .build();
    }
}