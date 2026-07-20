package com.aha.domain.auth.oauth2.info;

import java.util.Map;

//Kakao OAuth2 중첩 응답 변환

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(
            Map<String, Object> attributes
    ) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");

        if (id == null) {
            return null;
        }

        return String.valueOf(id);
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

        return String.valueOf(email);
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

        Object nickname = profile.get("nickname");

        if (nickname == null) {
            return null;
        }

        return String.valueOf(nickname);
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

        if (profileImageUrl == null) {
            return null;
        }

        return String.valueOf(profileImageUrl);
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