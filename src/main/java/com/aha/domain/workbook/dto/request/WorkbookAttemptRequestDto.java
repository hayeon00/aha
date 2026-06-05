package com.aha.domain.workbook.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class WorkbookAttemptRequestDto {

    @NotNull(message = "워크북 아이디는 필수입니다.")
    private Long workbookId;
}