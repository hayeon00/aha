package com.aha.domain.user.entity;

import jakarta.persistence.*;
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

  @Column(nullable = false, length = 100)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(nullable = false, length = 20)
  private String role;

  @Column(nullable = false, length = 20)
  private String status;

  @Column(name = "login_type", nullable = false, length = 20)
  private String loginType;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "is_email_verified", nullable = false)
  private boolean isEmailVerified;

  @Column(name = "profile_image_url", length = 255)
  private String profileImageUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public User(String email, String password, String name, String nickname,
      String role, String status, String loginType, String profileImageUrl) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.nickname = nickname;
    this.role = (role != null) ? role : "USER";
    this.status = (status != null) ? status : "ACTIVE";
    this.loginType = (loginType != null) ? loginType : "LOCAL";
    this.isEmailVerified = false; // 기본값 FALSE
    this.profileImageUrl = profileImageUrl;
  }

  public void updateProfile(String nickname, String profileImageUrl) {
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

  public void verifyEmail() {
    this.isEmailVerified = true;
  }

  public void updateLastLoginAt() {
    this.lastLoginAt = LocalDateTime.now();
  }

  public void changeStatus(String status) {
    this.status = status;
  }
}