package com.aha.domain.auth.oauth2.service;

import com.aha.domain.auth.enums.SocialProvider;
import com.aha.domain.auth.oauth2.info.OAuth2UserInfo;
import com.aha.domain.auth.oauth2.info.OAuth2UserInfoFactory;
import com.aha.domain.auth.oauth2.principal.CustomOAuth2User;
import com.aha.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final SocialLoginProcessor socialLoginProcessor;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId();

        User user;

        try {
            SocialProvider provider =
                    OAuth2UserInfoFactory.resolveProvider(
                            registrationId
                    );

            OAuth2UserInfo userInfo =
                    OAuth2UserInfoFactory.create(
                            registrationId,
                            oauth2User.getAttributes()
                    );

            user = socialLoginProcessor.process(
                    provider,
                    userInfo
            );
        } catch (OAuth2AuthenticationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(
                            "social_login_processing_failed"
                    ),
                    exception
            );
        }

        String nameAttributeKey =
                userRequest
                        .getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();

        return new CustomOAuth2User(
                user,
                oauth2User.getAttributes(),
                nameAttributeKey
        );
    }
}
