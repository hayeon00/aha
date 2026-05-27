package com.aha.domain.user.controller;

import com.aha.domain.user.dto.response.MyInfoResponseDto;
import com.aha.domain.user.service.UserService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<MyInfoResponseDto> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyInfoResponseDto response = userService.getMyInfo(userDetails.getId());

        return ApiResponse.success(
                200,
                "내 정보 조회에 성공했습니다.",
                response
        );
    }
}