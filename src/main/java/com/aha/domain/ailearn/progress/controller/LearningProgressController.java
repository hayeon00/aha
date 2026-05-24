package com.aha.domain.ailearn.progress.controller;

import com.aha.domain.ailearn.progress.dto.response.LearningProgressSummaryResponse;
import com.aha.domain.ailearn.progress.service.LearningProgressService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning/progress")
@RequiredArgsConstructor
public class LearningProgressController {

    private final LearningProgressService learningProgressService;

    @GetMapping("/summary")
    public ApiResponse<LearningProgressSummaryResponse> getProgressSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long examVersionId
    ) {

        LearningProgressSummaryResponse response =
                learningProgressService.getProgressSummary(
                        userDetails.getId(),
                        examVersionId
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "학습 진도 조회에 성공했습니다.",
                response
        );
    }
}