package com.aha.domain.exam.controller;

import com.aha.domain.exam.dto.response.ExamScopeNodeResponse;
import com.aha.domain.exam.service.ExamScopeNodeService;
import com.aha.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exam-versions")
@RequiredArgsConstructor
public class ExamScopeNodeController {

    private final ExamScopeNodeService examScopeNodeService;

    @GetMapping("/{examVersionId}/syllabus")
    public ApiResponse<List<ExamScopeNodeResponse>> getSyllabus(
            @PathVariable Long examVersionId
    ) {
        List<ExamScopeNodeResponse> response =
                examScopeNodeService.getSyllabusTree(examVersionId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "시험 버전별 목차 조회에 성공했습니다.",
                response
        );
    }
}