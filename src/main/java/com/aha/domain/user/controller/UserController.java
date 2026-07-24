package com.aha.domain.user.controller;

import com.aha.domain.user.dto.request.UpdateProfileRequestDto;
import com.aha.domain.user.dto.response.MyInfoResponseDto;
import com.aha.domain.user.service.UserService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyInfoResponseDto response =
                userService.getMyInfo(userDetails.getId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "내 정보 조회에 성공했습니다.",
                        response
                )
        );
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<MyInfoResponseDto>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDto request
    ) {
        MyInfoResponseDto response =
                userService.updateProfile(
                        userDetails.getId(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "프로필 정보 수정에 성공했습니다.",
                        response
                )
        );
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<MyInfoResponseDto>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        MyInfoResponseDto response =
                userService.updateProfileImage(
                        userDetails.getId(),
                        profileImage
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "프로필 이미지 수정에 성공했습니다.",
                        response
                )
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.withdrawUser(
                userDetails.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "회원 탈퇴가 완료되었습니다.",
                        null
                )
        );
    }
}
