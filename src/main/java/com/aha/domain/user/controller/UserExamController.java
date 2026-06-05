package com.aha.domain.user.controller;

import com.aha.domain.user.dto.request.UserExamHiddenUpdateRequestDto;
import com.aha.domain.user.dto.response.UserExamResponseDto;
import com.aha.domain.user.service.UserExamService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-exams")
@RequiredArgsConstructor
public class UserExamController {

    private final UserExamService userExamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserExamResponseDto>>> getMyUserExams(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserExamResponseDto> response =
                userExamService.getMyUserExams(userDetails.getId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "내 시험 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/visible")
    public ResponseEntity<ApiResponse<List<UserExamResponseDto>>> getMyVisibleUserExams(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserExamResponseDto> response =
                userExamService.getMyVisibleUserExams(userDetails.getId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "표시 중인 내 시험 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @PatchMapping("/{userExamId}/hidden")
    public ResponseEntity<ApiResponse<UserExamResponseDto>> updateHidden(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userExamId,
            @Valid @RequestBody UserExamHiddenUpdateRequestDto request
    ) {
        UserExamResponseDto response =
                userExamService.updateHidden(
                        userDetails.getId(),
                        userExamId,
                        request.hidden()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "시험 표시 설정 변경에 성공했습니다.",
                        response
                )
        );
    }
}