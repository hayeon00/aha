package com.aha.domain.ailearn.document.controller;

import com.aha.domain.ailearn.document.dto.content.response.UserLearningContentResponseDto;
import com.aha.domain.ailearn.document.service.content.UserLearningContentQueryService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-learning/learning-contents")
@RequiredArgsConstructor
public class UserLearningContentController {

    private final UserLearningContentQueryService userLearningContentQueryService;

    @GetMapping("/user-exams/{userExamId}")
    public ResponseEntity<ApiResponse<List<UserLearningContentResponseDto>>> getLearningContents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId
    ) {
        List<UserLearningContentResponseDto> response =
                userLearningContentQueryService
                        .getLearningContents(
                                userDetails.getId(),
                                userExamId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "개념설명 목록 조회에 성공했습니다.",
                        response
                )
        );
    }


    @GetMapping("/user-exams/{userExamId}/topics/{topicId}")
    public ResponseEntity<ApiResponse<UserLearningContentResponseDto>> getLearningContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @PathVariable Long topicId
    ) {
        UserLearningContentResponseDto response =
                userLearningContentQueryService
                        .getLearningContent(
                                userDetails.getId(),
                                userExamId,
                                topicId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "개념설명 조회에 성공했습니다.",
                        response
                )
        );
    }
}