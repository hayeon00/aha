package com.aha.domain.auth.oauth2.info;

import com.aha.domain.auth.enums.SocialProvider;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;

import java.util.Locale;
import java.util.Map;

// 어떤 구현제(kakao, google)를 사용할것인지 결정

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo create(
            String registrationId,
            Map<String, Object> attributes
    ) {
        SocialProvider provider =
                resolveProvider(registrationId);

        return switch (provider) {
            case GOOGLE ->
                    new GoogleOAuth2UserInfo(attributes);

            case KAKAO ->
                    new KakaoOAuth2UserInfo(attributes);
        };
    }

    public static SocialProvider resolveProvider(
            String registrationId
    ) {
        if (registrationId == null
                || registrationId.isBlank()) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER
            );
        }

        try {
            return SocialProvider.valueOf(
                    registrationId
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER
            );
        }
    }
}