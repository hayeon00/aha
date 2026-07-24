package com.aha.domain.auth.oauth2.info;

import java.util.Locale;
import java.util.Map;

public class KakaoOAuth2UserInfo
        implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(
            Map<String, Object> attributes
    ) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");

        return id == null
                ? null
                : String.valueOf(id);
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount =
                getMap(attributes, "kakao_account");

        if (kakaoAccount == null) {
            return null;
        }

        Object email = kakaoAccount.get("email");

        if (email == null) {
            return null;
        }

        String normalizedEmail =
                String.valueOf(email)
                        .trim()
                        .toLowerCase(Locale.ROOT);

        return normalizedEmail.isBlank()
                ? null
                : normalizedEmail;
    }

    @Override
    public boolean isEmailVerified() {
        Map<String, Object> kakaoAccount =
                getMap(attributes, "kakao_account");

        if (kakaoAccount == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                kakaoAccount.get("is_email_valid")
        ) && Boolean.TRUE.equals(
                kakaoAccount.get("is_email_verified")
        );
    }

    @Override
    public String getName() {
        return getNickname();
    }

    @Override
    public String getNickname() {
        Map<String, Object> kakaoAccount =
                getMap(attributes, "kakao_account");

        if (kakaoAccount == null) {
            return null;
        }

        Map<String, Object> profile =
                getMap(kakaoAccount, "profile");

        if (profile == null) {
            return null;
        }

        Object nickname =
                profile.get("nickname");

        return nickname == null
                ? null
                : String.valueOf(nickname);
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> kakaoAccount =
                getMap(attributes, "kakao_account");

        if (kakaoAccount == null) {
            return null;
        }

        Map<String, Object> profile =
                getMap(kakaoAccount, "profile");

        if (profile == null) {
            return null;
        }

        Object profileImageUrl =
                profile.get("profile_image_url");

        return profileImageUrl == null
                ? null
                : String.valueOf(profileImageUrl);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(
            Map<String, Object> source,
            String key
    ) {
        Object value = source.get(key);

        if (!(value instanceof Map<?, ?>)) {
            return null;
        }

        return (Map<String, Object>) value;
    }
}