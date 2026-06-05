package com.aha.domain.workbook.controller;


import com.aha.domain.workbook.dto.response.ExamWorkbookTypeResponseDto;
import com.aha.domain.workbook.service.ExamWorkbookTypeService;
import com.aha.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exam-workbook-types")
@RequiredArgsConstructor
public class ExamWorkbookTypeController {

    private final ExamWorkbookTypeService examWorkbookTypeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ExamWorkbookTypeResponseDto>>> getExamWorkbookType(
        @RequestParam("examCode") String examCode
    ) {
        List<ExamWorkbookTypeResponseDto> responseDtoList = examWorkbookTypeService.getExamWorkbookType(examCode);
        return ResponseEntity.ok(
            ApiResponse.success(200, "해당 시험 지원 가능 워크북 유형 조회 성공", responseDtoList)
        );
    }

}
