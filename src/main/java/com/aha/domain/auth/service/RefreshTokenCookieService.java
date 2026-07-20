package com.aha.domain.auth.service;

import com.aha.domain.auth.constants.AuthCookieNames;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieService {

    private final boolean secure;
    private final Duration refreshTokenMaxAge;

    public RefreshTokenCookieService(
            @Value("${auth.cookie.secure:false}")
            boolean secure,
            @Value("${auth.cookie.refresh-token-max-age-seconds:1209600}")
            long refreshTokenMaxAgeSeconds
    ) {
        this.secure = secure;
        this.refreshTokenMaxAge =
                Duration.ofSeconds(refreshTokenMaxAgeSeconds);
    }

    public void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken
    ) {
        ResponseCookie cookie = ResponseCookie
                .from(
                        AuthCookieNames.REFRESH_TOKEN,
                        refreshToken
                )
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(refreshTokenMaxAge)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    public void expireRefreshTokenCookie(
            HttpServletResponse response
    ) {
        ResponseCookie cookie = ResponseCookie
                .from(
                        AuthCookieNames.REFRESH_TOKEN,
                        ""
                )
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}