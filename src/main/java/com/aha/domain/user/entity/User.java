package com.aha.domain.user.entity;

import com.aha.domain.user.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private User(
            String email,
            String password,
            String name,
            String nickname,
            UserRole role,
            String status,
            boolean isEmailVerified,
            String profileImageUrl
    ) {
        this.email = normalizeEmail(email);
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : "ACTIVE";
        this.isEmailVerified = isEmailVerified;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateProfileInfo(
            String name,
            String nickname
    ) {
        this.name = name;
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateEmail(String email) {
        this.email = normalizeEmail(email);
        this.isEmailVerified = false;
    }

    public void verifyEmail() {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "인증할 이메일이 등록되어 있지 않습니다."
            );
        }

        this.isEmailVerified = true;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}