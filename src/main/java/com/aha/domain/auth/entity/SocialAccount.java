package com.aha.domain.auth.entity;

import com.aha.domain.auth.enums.SocialProvider;
import com.aha.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_account_provider_provider_id",
                        columnNames = {
                                "provider",
                                "provider_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_social_account_user_provider",
                        columnNames = {
                                "user_id",
                                "provider"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "provider_email", length = 100)
    private String providerEmail;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private SocialAccount(
            User user,
            SocialProvider provider,
            String providerId,
            String providerEmail,
            String profileImageUrl
    ) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.providerEmail =
                normalizeEmail(providerEmail);
        this.profileImageUrl = profileImageUrl;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateLoginInfo(String providerEmail, String profileImageUrl) {
        this.providerEmail = normalizeEmail(providerEmail);
        this.profileImageUrl = profileImageUrl;
        this.lastLoginAt = LocalDateTime.now();
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}