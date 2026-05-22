package com.aha.domain.ailearn.content.controller;

import com.aha.domain.ailearn.content.dto.LearningContentResponse;
import com.aha.domain.ailearn.content.service.LearningContentService;
import com.aha.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning/contents")
@RequiredArgsConstructor
public class LearningContentController {

    private final LearningContentService learningContentService;

    @GetMapping("/{scopeNodeId}")
    public ApiResponse<LearningContentResponse> getLearningContent(
            @PathVariable Long scopeNodeId
    ) {
        LearningContentResponse response = learningContentService.getLearningContent(scopeNodeId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "소목차별 개념 설명 조회에 성공했습니다.",
                response
        );
    }
}