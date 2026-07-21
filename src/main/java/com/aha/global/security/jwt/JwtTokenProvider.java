package com.aha.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(
            Long userId,
            String email,
            String role
    ) {
        Date now = new Date();

        Date expiration = new Date(
                now.getTime()
                        + jwtProperties.getAccessTokenExpiration()
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(
                        TOKEN_TYPE_CLAIM,
                        JwtTokenType.ACCESS.name()
                )
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();

        Date expiration = new Date(
                now.getTime()
                        + jwtProperties.getRefreshTokenExpiration()
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(
                        TOKEN_TYPE_CLAIM,
                        JwtTokenType.REFRESH.name()
                )
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    public Long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }

    public boolean validateAccessToken(String token) {
        return validateTokenType(
                token,
                JwtTokenType.ACCESS
        );
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(
                token,
                JwtTokenType.REFRESH
        );
    }

    public Long getUserId(String token) {
        return Long.valueOf(
                getClaims(token).getSubject()
        );
    }

    public String getEmail(String token) {
        return getClaims(token)
                .get("email", String.class);
    }

    public String getRole(String token) {
        return getClaims(token)
                .get("role", String.class);
    }

    public JwtTokenType getTokenType(String token) {
        String tokenType = getClaims(token)
                .get(
                        TOKEN_TYPE_CLAIM,
                        String.class
                );

        return JwtTokenType.valueOf(tokenType);
    }

    private boolean validateTokenType(
            String token,
            JwtTokenType expectedTokenType
    ) {
        try {
            Claims claims = getClaims(token);

            Date expiration = claims.getExpiration();

            if (expiration == null
                    || !expiration.after(new Date())) {
                return false;
            }

            String actualTokenType = claims.get(
                    TOKEN_TYPE_CLAIM,
                    String.class
            );

            return expectedTokenType.name()
                    .equals(actualTokenType);

        } catch (Exception exception) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}