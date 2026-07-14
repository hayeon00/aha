package com.aha.domain.workbook.controller;

import com.aha.domain.workbook.dto.response.AttemptStartResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookItemResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookResponseDto;
import com.aha.domain.workbook.enums.WorkbookTypeCode;
import com.aha.domain.workbook.service.WorkbookService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        @Min(1) @PathVariable(name = "examVersionId") Long examVersionId,
        @RequestParam(name = "workbookTypeCode", required = false, defaultValue = "PAST") WorkbookTypeCode workbookTypeCode
    ) {
        List<WorkbookResponseDto> response = workbookService.getWorkbooks(examVersionId,
            workbookTypeCode);
        return ResponseEntity.ok()
            .body(ApiResponse.success(200, workbookTypeCode.getName() + "워크북 목록 조회 성공", response));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/workbooks/{workbookId}/attempts")
    public ResponseEntity<ApiResponse<AttemptStartResponseDto>> startAttempt(
        @Min(1) @PathVariable Long workbookId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        AttemptStartResponseDto response = workbookService.startAttempt(workbookId,userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(201,"풀이를 시작했습니다.",response));
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/workbooks/{workbookId}/items")
    public ResponseEntity<ApiResponse<List<WorkbookItemResponseDto>>> getWorkbookItems(
        @Min(1) @PathVariable Long workbookId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<WorkbookItemResponseDto> response = workbookService.getWorkbookItems(workbookId,userDetails);
        return ResponseEntity.ok().body(ApiResponse.success(200,"워크북 문항 목록 조회 성공",response));
    }



}
