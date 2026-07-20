package com.aha.domain.auth.oauth2.info;

// 공통 사용자 정보 인터페이스

public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getName();

    String getNickname();

    String getProfileImageUrl();
}