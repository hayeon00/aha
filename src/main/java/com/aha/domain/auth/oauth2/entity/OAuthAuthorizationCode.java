package com.aha.domain.auth.oauth2.entity;

import com.aha.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oauth_authorization_code",
        indexes = {
                @Index(
                        name = "idx_oauth_authorization_code_hash",
                        columnList = "code_hash",
                        unique = true
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAuthorizationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_hash", nullable = false, length = 64, unique = true)
    private String codeHash;

    @Column(name = "session_id_hash", nullable = false, length = 64)
    private String sessionIdHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private OAuthAuthorizationCode(
            String codeHash,
            String sessionIdHash,
            User user,
            LocalDateTime expiresAt
    ) {
        this.codeHash = codeHash;
        this.sessionIdHash = sessionIdHash;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public static OAuthAuthorizationCode create(
            String codeHash,
            String sessionIdHash,
            User user,
            LocalDateTime expiresAt
    ) {
        return new OAuthAuthorizationCode(
                codeHash,
                sessionIdHash,
                user,
                expiresAt
        );
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed(LocalDateTime now) {
        if (isUsed()) {
            throw new IllegalStateException(
                    "이미 사용된 OAuth 인증 코드입니다."
            );
        }

        this.usedAt = now;
    }
}