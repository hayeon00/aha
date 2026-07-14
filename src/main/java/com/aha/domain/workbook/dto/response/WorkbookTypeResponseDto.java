package com.aha.domain.workbook.dto.response;

import com.aha.domain.workbook.entity.WorkbookType;
import com.aha.domain.workbook.enums.WorkbookTypeCode;

public record WorkbookTypeResponseDto(
    WorkbookTypeCode code,
    String name,
    Integer displayOrder
) {
    public static WorkbookTypeResponseDto from(WorkbookType workbookType){
        return new WorkbookTypeResponseDto(
            workbookType.getCode(),
            workbookType.getName(),
            workbookType.getDisplayOrder()
        );
    }
}
