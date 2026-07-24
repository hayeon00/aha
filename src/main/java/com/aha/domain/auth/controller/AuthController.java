package com.aha.domain.auth.controller;

import com.aha.domain.auth.constants.AuthCookieNames;
import com.aha.domain.auth.dto.request.LoginRequestDto;
import com.aha.domain.auth.dto.request.OAuthCodeExchangeRequestDto;
import com.aha.domain.auth.dto.request.SignupRequestDto;
import com.aha.domain.auth.dto.response.AccessTokenResponseDto;
import com.aha.domain.auth.dto.response.SignupResponseDto;
import com.aha.domain.auth.oauth2.service.OAuthCodeExchangeService;
import com.aha.domain.auth.service.AuthService;
import com.aha.domain.auth.service.IssuedTokens;
import com.aha.domain.auth.service.RefreshTokenCookieService;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final OAuthCodeExchangeService oauthCodeExchangeService;

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
    public ResponseEntity<ApiResponse<AccessTokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        IssuedTokens issuedTokens = authService.login(request);

        refreshTokenCookieService
                .addRefreshTokenCookie(
                        response,
                        issuedTokens.refreshToken()
                );

        AccessTokenResponseDto responseDto =
                AccessTokenResponseDto.from(
                        issuedTokens
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "로그인에 성공했습니다.",
                        responseDto
                )
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AccessTokenResponseDto>> reissue(
            @CookieValue(name = AuthCookieNames.REFRESH_TOKEN, required = false)
            String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity
                    .noContent()
                    .build();
        }

        IssuedTokens issuedTokens =
                authService.reissue(refreshToken);

        refreshTokenCookieService.addRefreshTokenCookie(
                response,
                issuedTokens.refreshToken()
        );

        AccessTokenResponseDto responseDto =
                AccessTokenResponseDto.from(
                        issuedTokens
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "토큰이 재발급되었습니다.",
                        responseDto
                )
        );
    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<ApiResponse<AccessTokenResponseDto>>
    exchangeOAuthCode(
            @Valid
            @RequestBody
            OAuthCodeExchangeRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
            );
        }

        IssuedTokens issuedTokens =
                oauthCodeExchangeService.exchange(
                        requestDto.code(),
                        session.getId()
                );

        refreshTokenCookieService
                .addRefreshTokenCookie(
                        response,
                        issuedTokens.refreshToken()
                );

        session.invalidate();
        SecurityContextHolder.clearContext();

        AccessTokenResponseDto responseDto =
                new AccessTokenResponseDto(
                        issuedTokens.accessToken(),
                        issuedTokens.accessTokenExpiresIn()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "소셜 로그인에 성공했습니다.",
                        responseDto
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = AuthCookieNames.REFRESH_TOKEN, required = false)
            String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);

        refreshTokenCookieService
                .expireRefreshTokenCookie(response);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "로그아웃되었습니다.",
                        null
                )
        );
    }
}
