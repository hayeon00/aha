package com.aha.domain.auth.service;

public record IssuedTokens(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {
}