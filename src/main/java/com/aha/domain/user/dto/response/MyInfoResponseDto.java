package com.aha.domain.user.dto.response;

import com.aha.domain.user.entity.User;

public record MyInfoResponseDto(
        Long userId,
        String email,
        String nickname
) {
    public static MyInfoResponseDto from(User user) {
        return new MyInfoResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}