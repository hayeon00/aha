package com.aha.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final Long accessTokenExpiresIn;
    private final Long refreshTokenExpiresIn;

    public LoginResponseDto(
            String accessToken,
            String refreshToken,
            Long accessTokenExpiresIn,
            Long refreshTokenExpiresIn
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }
}