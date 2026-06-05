package com.aha.domain.exam.controller;

import com.aha.domain.exam.dto.response.ExamResponseDto;
import com.aha.domain.exam.service.ExamService;
import com.aha.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamResponseDto>>> getSupportedExams() {
        List<ExamResponseDto> response = examService.getSupportedExams();

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "지원 시험 목록 조회에 성공했습니다.",
                        response
                )
        );
    }
}