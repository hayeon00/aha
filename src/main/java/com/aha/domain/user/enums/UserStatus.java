package com.aha.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("정지"),
    WITHDRAWN("탈퇴");

    private final String description;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}