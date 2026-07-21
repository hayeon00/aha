package com.aha.domain.user.controller;

import com.aha.domain.user.service.UserService;
import com.aha.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @PathVariable Long userId
    ) {
        userService.suspendUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "사용자 계정이 정지되었습니다.",
                        null
                )
        );
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(
            @PathVariable Long userId
    ) {
        userService.activateUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "사용자 계정이 활성화되었습니다.",
                        null
                )
        );
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable Long userId
    ) {
        userService.deactivateUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "사용자 계정이 비활성화되었습니다.",
                        null
                )
        );
    }
}