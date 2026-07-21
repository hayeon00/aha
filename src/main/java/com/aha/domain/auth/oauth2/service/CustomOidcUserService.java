package com.aha.domain.auth.oauth2.service;

import com.aha.domain.auth.enums.SocialProvider;
import com.aha.domain.auth.oauth2.info.OAuth2UserInfo;
import com.aha.domain.auth.oauth2.info.OAuth2UserInfoFactory;
import com.aha.domain.auth.oauth2.principal.CustomOidcUser;
import com.aha.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final SocialLoginProcessor socialLoginProcessor;

    @Override
    public OidcUser loadUser(
            OidcUserRequest userRequest
    ) throws OAuth2AuthenticationException {

        OidcUser oidcUser =
                super.loadUser(userRequest);

        String registrationId =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId();

        SocialProvider provider =
                OAuth2UserInfoFactory.resolveProvider(
                        registrationId
                );

        OAuth2UserInfo userInfo =
                OAuth2UserInfoFactory.create(
                        registrationId,
                        oidcUser.getClaims()
                );

        User user = socialLoginProcessor.process(
                provider,
                userInfo
        );

        return new CustomOidcUser(
                user,
                oidcUser
        );
    }
}