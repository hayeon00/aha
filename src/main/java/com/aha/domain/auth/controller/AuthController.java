package com.aha.domain.auth.controller;

import com.aha.domain.auth.dto.request.LoginRequestDto;
import com.aha.domain.auth.dto.request.SignupRequestDto;
import com.aha.domain.auth.dto.response.LoginResponseDto;
import com.aha.domain.auth.dto.response.SignupResponseDto;
import com.aha.domain.auth.service.AuthService;
import com.aha.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {
        SignupResponseDto response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        201,
                        "회원가입이 완료되었습니다.",
                        response
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        LoginResponseDto response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "로그인에 성공했습니다.",
                        response
                )
        );
    }
}