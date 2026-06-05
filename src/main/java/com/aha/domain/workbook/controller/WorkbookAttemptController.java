package com.aha.domain.workbook.controller;

import com.aha.domain.workbook.dto.request.WorkbookAttemptRequestDto;
import com.aha.domain.workbook.dto.response.WorkbookAttemptResponseDto;
import com.aha.domain.workbook.service.WorkbookAttemptService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/workbook-attempts")
@RequiredArgsConstructor
public class WorkbookAttemptController {

    private final WorkbookAttemptService workbookAttemptService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkbookAttemptResponseDto>> startWorkbook(
        @RequestBody WorkbookAttemptRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        WorkbookAttemptResponseDto responseDto =  workbookAttemptService.startWorkbook(requestDto.getWorkbookId(),userDetails);

        return ResponseEntity.ok(
            ApiResponse.success(200,"워크북 풀이 시작",responseDto)
        );
    }
}
