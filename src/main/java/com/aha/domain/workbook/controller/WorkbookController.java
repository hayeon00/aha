package com.aha.domain.workbook.controller;

import com.aha.domain.workbook.dto.response.WorkbookGroupResponseDto;
import com.aha.domain.workbook.service.WorkbookService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workbooks")
@RequiredArgsConstructor
public class WorkbookController {

    private final WorkbookService workbookService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkbookGroupResponseDto>> getWorkbookList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("examCode") String examCode,
        @RequestParam("workbookTypeCode") String workbookTypeCode
    ) {
        WorkbookGroupResponseDto responseDto = workbookService.getWorkbookList(userDetails.getId(), examCode, workbookTypeCode);

        return ResponseEntity.ok(
            ApiResponse.success(200, examCode + " 워크북 목록 조회 성공", responseDto)
        );
    }
}
