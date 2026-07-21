package com.aha.domain.user.dto.response;

import com.aha.domain.user.entity.User;
import com.aha.domain.user.enums.UserStatus;

import java.time.LocalDateTime;

public record MyInfoResponseDto(
        Long id,
        String email,
        String name,
        String nickname,
        String role,
        UserStatus status,
        Boolean emailVerified,
        String profileImageUrl,
        Boolean examOnboardingCompleted,
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
                user.isEmailVerified(),
                user.getProfileImageUrl(),
                user.isExamOnboardingCompleted(),
                user.getCreatedAt()
        );
    }
}
