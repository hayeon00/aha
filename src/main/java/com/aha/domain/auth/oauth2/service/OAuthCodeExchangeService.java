package com.aha.domain.auth.oauth2.service;

import com.aha.domain.auth.service.AuthTokenService;
import com.aha.domain.auth.service.IssuedTokens;
import com.aha.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthCodeExchangeService {

    private final OAuthAuthorizationCodeService
            authorizationCodeService;

    private final AuthTokenService authTokenService;

    @Transactional
    public IssuedTokens exchange(
            String code,
            String sessionId
    ) {
        User user =
                authorizationCodeService.consumeCode(
                        code,
                        sessionId
                );

        return authTokenService.issueTokens(user);
    }
}