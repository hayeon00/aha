package com.aha.domain.workbook.controller;

import com.aha.domain.workbook.dto.response.WorkbookResponseDto;
import com.aha.domain.workbook.entity.WorkbookTypeCode;
import com.aha.domain.workbook.service.WorkbookService;
import com.aha.global.response.ApiResponse;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WorkbookController {

    private final WorkbookService workbookService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exam-versions/{examVersionId}/workbooks")
    public ResponseEntity<ApiResponse<List<WorkbookResponseDto>>> getWorkbooks(
        @PathVariable(name = "examVersionId") @Min(1) Long examVersionId,
        @RequestParam(name = "workbookTypeCode", required = false, defaultValue = "PAST") WorkbookTypeCode workbookTypeCode
    ) {
        List<WorkbookResponseDto> response = workbookService.getWorkbooks(examVersionId,
            workbookTypeCode);
        return ResponseEntity.ok()
            .body(ApiResponse.success(200, workbookTypeCode.getName() + "워크북 목록 조회 성공", response));
    }

}
