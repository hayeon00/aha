package com.aha.domain.auth.oauth2.info;

import java.util.Map;

// Google OIDC Claims 변환

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(
            Map<String, Object> attributes
    ) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return getString("sub");
    }

    @Override
    public String getEmail() {
        return getString("email");
    }

    @Override
    public String getName() {
        return getString("name");
    }

    @Override
    public String getNickname() {
        return getString("name");
    }

    @Override
    public String getProfileImageUrl() {
        return getString("picture");
    }

    private String getString(String key) {
        Object value = attributes.get(key);

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }
}