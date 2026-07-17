package com.aha.domain.workbook.controller;

import com.aha.domain.workbook.dto.response.WorkbookTypeResponseDto;
import com.aha.domain.workbook.service.WorkbookTypeService;
import com.aha.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workbook-types")
public class WorkbookTypeController {

    private final WorkbookTypeService workbookTypeService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkbookTypeResponseDto>>> getWorkbookTypes(){
        List<WorkbookTypeResponseDto> response = workbookTypeService.getWorkbookTypes();

        return ResponseEntity.ok().body(ApiResponse.success(200,"워크북 유형 목록 조회 성공",response));
    }


}
