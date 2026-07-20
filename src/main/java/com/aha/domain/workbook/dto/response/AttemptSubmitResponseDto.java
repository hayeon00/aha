package com.aha.domain.workbook.dto.response;

import com.aha.domain.workbook.entity.WorkbookAttempt;
import com.aha.domain.workbook.enums.AttemptStatus;
import lombok.Builder;

@Builder
public record AttemptSubmitResponseDto(

    Long id,
    AttemptStatus status
) {

    public static AttemptSubmitResponseDto from(WorkbookAttempt workbookAttempt){

        return AttemptSubmitResponseDto.builder()
            .id(workbookAttempt.getId())
            .status(workbookAttempt.getStatus())
            .build();
    }
}
