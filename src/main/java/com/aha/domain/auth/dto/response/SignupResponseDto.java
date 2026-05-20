package com.aha.domain.auth.dto.response;

import com.aha.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SignupResponseDto {

    private final Long userId;
    private final String email;
    private final String name;
    private final String nickname;
    private final String role;
    private final String status;
    private final String loginType;
    private final boolean emailVerified;
    private final LocalDateTime createdAt;

    public SignupResponseDto(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.loginType = user.getLoginType();
        this.emailVerified = user.isEmailVerified();
        this.createdAt = user.getCreatedAt();
    }
}