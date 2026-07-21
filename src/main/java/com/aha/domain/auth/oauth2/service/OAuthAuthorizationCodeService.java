package com.aha.domain.auth.oauth2.service;

import com.aha.domain.auth.oauth2.entity.OAuthAuthorizationCode;
import com.aha.domain.auth.oauth2.repository.OAuthAuthorizationCodeRepository;
import com.aha.domain.user.entity.User;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthAuthorizationCodeService {

    private static final long CODE_EXPIRATION_SECONDS = 60L;
    private static final int CODE_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    private final OAuthAuthorizationCodeRepository
            authorizationCodeRepository;

    @Transactional
    public String createCode(
            User user,
            String sessionId
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
            );
        }

        String rawCode = generateRandomCode();

        OAuthAuthorizationCode authorizationCode =
                OAuthAuthorizationCode.create(
                        hash(rawCode),
                        hash(sessionId),
                        user,
                        LocalDateTime.now()
                                .plusSeconds(
                                        CODE_EXPIRATION_SECONDS
                                )
                );

        authorizationCodeRepository.save(
                authorizationCode
        );

        return rawCode;
    }

    @Transactional
    public User consumeCode(
            String rawCode,
            String sessionId
    ) {
        if (rawCode == null
                || rawCode.isBlank()
                || sessionId == null
                || sessionId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
            );
        }

        OAuthAuthorizationCode authorizationCode =
                authorizationCodeRepository
                        .findByCodeHashForUpdate(
                                hash(rawCode)
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
                                )
                        );

        LocalDateTime now = LocalDateTime.now();

        if (authorizationCode.isUsed()) {
            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
            );
        }

        if (authorizationCode.isExpired(now)) {
            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
            );
        }

        if (!MessageDigest.isEqual(
                authorizationCode
                        .getSessionIdHash()
                        .getBytes(StandardCharsets.UTF_8),
                hash(sessionId)
                        .getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE
            );
        }

        authorizationCode.markUsed(now);

        return authorizationCode.getUser();
    }

    private String generateRandomCode() {
        byte[] bytes =
                new byte[CODE_BYTE_LENGTH];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] digest =
                    messageDigest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }
    }
}