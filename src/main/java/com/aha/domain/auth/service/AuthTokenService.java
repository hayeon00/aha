package com.aha.domain.auth.service;

import com.aha.domain.auth.entity.RefreshToken;
import com.aha.domain.auth.repository.RefreshTokenRepository;
import com.aha.domain.user.entity.User;
import com.aha.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public IssuedTokens issueTokens(User user) {

        String accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name()
                );

        String refreshToken =
                jwtTokenProvider.createRefreshToken(
                        user.getId()
                );

        saveOrUpdateRefreshToken(
                user,
                refreshToken
        );

        return new IssuedTokens(
                accessToken,
                refreshToken,
                jwtTokenProvider
                        .getAccessTokenExpirationSeconds()
        );
    }

    private void saveOrUpdateRefreshToken(
            User user,
            String refreshToken
    ) {
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        savedToken ->
                                savedToken.updateToken(
                                        refreshToken
                                ),
                        () ->
                                refreshTokenRepository.save(
                                        new RefreshToken(
                                                user,
                                                refreshToken
                                        )
                                )
                );
    }
}