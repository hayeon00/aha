package com.aha.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private final String accessToken;
    private final String tokenType;
    private final Long expiresIn;

    public LoginResponseDto(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }
}