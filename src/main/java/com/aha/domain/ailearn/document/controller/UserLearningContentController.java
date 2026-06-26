package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.UserLearningContentResponseDto;
import com.aha.domain.ailearn.document.service.content.UserLearningContentQueryService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : UserLearningContentController
 * @since : 2026. 6. 25. 목요일
 */

@RestController
@RequestMapping("/api/v1/ai-learning/learning-contents")
@RequiredArgsConstructor
public class UserLearningContentController {

    private final UserLearningContentQueryService userLearningContentQueryService;

    @GetMapping("/user-exams/{userExamId}/scope-nodes/{examScopeNodeId}")
    public ResponseEntity<ApiResponse<UserLearningContentResponseDto>>
    getLearningContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @PathVariable Long examScopeNodeId) {

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
