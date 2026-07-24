package com.aha.domain.auth.service;

import com.aha.domain.user.entity.User;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UserAccountStatusValidator {

    public void validate(User user) {
        if (user == null
                || !user.getStatus().canAuthenticate()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_ACTIVE
            );
        }
    }
}