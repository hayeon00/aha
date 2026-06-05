package com.aha.domain.exam.controller;

import com.aha.domain.exam.dto.response.ExamScopeNodeResponseDto;
import com.aha.domain.exam.service.ExamScopeNodeService;
import com.aha.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-versions")
@RequiredArgsConstructor
public class ExamScopeNodeController {

    private final ExamScopeNodeService examScopeNodeService;

    @GetMapping("/{examVersionId}/scope-nodes")
    public ApiResponse<List<ExamScopeNodeResponseDto>> getScopeNodes(
            @PathVariable Long examVersionId
    ) {
        List<ExamScopeNodeResponseDto> response =
                examScopeNodeService.getSyllabusTree(examVersionId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "시험 목차 조회에 성공했습니다.",
                response
        );
    }
}