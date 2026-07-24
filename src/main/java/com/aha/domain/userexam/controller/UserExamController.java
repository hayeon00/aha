package com.aha.domain.userexam.controller;

import com.aha.domain.userexam.dto.request.ExamOnboardingRequestDto;
import com.aha.domain.userexam.dto.request.UserExamAddRequestDto;
import com.aha.domain.userexam.dto.request.UserExamHiddenUpdateRequestDto;
import com.aha.domain.userexam.dto.response.UserExamResponseDto;
import com.aha.domain.userexam.service.UserExamService;
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

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<List<UserExamResponseDto>>> completeOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExamOnboardingRequestDto request
    ) {
        List<UserExamResponseDto> response =
                userExamService.completeOnboarding(
                        userDetails.getId(),
                        request.examIds()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "선택된 시험이 저장되었습니다.",
                        response
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<UserExamResponseDto>>> addUserExams(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserExamAddRequestDto request
    ) {
        List<UserExamResponseDto> response =
                userExamService.addUserExams(
                        userDetails.getId(),
                        request.examIds()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "시험을 추가했습니다.",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserExamResponseDto>>> getMyUserExams(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserExamResponseDto> response =
                userExamService.getMyUserExams(
                        userDetails.getId()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "내 시험 목록을 조회했습니다.",
                        response
                )
        );
    }

    @GetMapping("/visible")
    public ResponseEntity<ApiResponse<List<UserExamResponseDto>>> getMyVisibleUserExams(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserExamResponseDto> response =
                userExamService.getMyVisibleUserExams(
                        userDetails.getId()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "표시 중인 시험 목록을 조회했습니다.",
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
                        "시험 표시 설정을 변경했습니다.",
                        response
                )
        );
    }
}