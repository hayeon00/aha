package com.aha.domain.auth.oauth2.info;

import java.util.Locale;
import java.util.Map;

public class GoogleOAuth2UserInfo
        implements OAuth2UserInfo {

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
        String email = getString("email");

        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(
                attributes.get("email_verified")
        );
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

        return value == null
                ? null
                : String.valueOf(value);
    }
}