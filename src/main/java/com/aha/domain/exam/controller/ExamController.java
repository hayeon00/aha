package com.aha.domain.exam.controller;

import com.aha.domain.exam.dto.response.ExamResponseDto;
import com.aha.domain.exam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public List<ExamResponseDto> getExams() {
        return examService.getActiveExams();
    }
}