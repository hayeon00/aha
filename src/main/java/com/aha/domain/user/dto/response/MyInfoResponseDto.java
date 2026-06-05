package com.aha.domain.user.dto.response;

import com.aha.domain.user.entity.User;

import java.time.LocalDateTime;

public record MyInfoResponseDto(
        Long id,
        String email,
        String name,
        String nickname,
        String role,
        String status,
        String loginType,
        Boolean emailVerified,
        String profileImageUrl,
        LocalDateTime createdAt
) {

    public static MyInfoResponseDto from(User user) {
        return new MyInfoResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus(),
                user.getLoginType(),
                user.isEmailVerified(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}