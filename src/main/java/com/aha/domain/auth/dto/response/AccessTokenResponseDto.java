package com.aha.domain.auth.dto.response;

import com.aha.domain.auth.service.IssuedTokens;

public record AccessTokenResponseDto(
        String accessToken,
        long accessTokenExpiresIn
) {
    public static AccessTokenResponseDto from(
            IssuedTokens issuedTokens
    ) {
        return new AccessTokenResponseDto(
                issuedTokens.accessToken(),
                issuedTokens.accessTokenExpiresIn()
        );
    }
}