package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.api.response.UserLearningContentResponseDto;
import com.aha.domain.ailearn.document.service.query.UserLearningContentQueryService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/aiLearn/learning-contents")
@RequiredArgsConstructor
public class UserLearningContentController {

    private final UserLearningContentQueryService
            userLearningContentQueryService;

    @GetMapping("/user-exams/{userExamId}/scope-nodes/{examScopeNodeId}")
    public ResponseEntity<ApiResponse<UserLearningContentResponseDto>>
    getLearningContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @PathVariable Long examScopeNodeId
    ) {
        UserLearningContentResponseDto response =
                userLearningContentQueryService
                        .getLearningContent(
                                userDetails.getId(),
                                userExamId,
                                examScopeNodeId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "개념 설명 조회에 성공했습니다.",
                        response
                )
        );
    }
}