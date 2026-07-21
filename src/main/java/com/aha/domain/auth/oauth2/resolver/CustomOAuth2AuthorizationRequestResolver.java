package com.aha.domain.auth.oauth2.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomOAuth2AuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_REQUEST_BASE_URI =
            "/oauth2/authorization";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.delegate =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        AUTHORIZATION_REQUEST_BASE_URI
                );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request
    ) {
        OAuth2AuthorizationRequest authorizationRequest =
                delegate.resolve(request);

        return customizeAuthorizationRequest(
                authorizationRequest
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request,
            String clientRegistrationId
    ) {
        OAuth2AuthorizationRequest authorizationRequest =
                delegate.resolve(
                        request,
                        clientRegistrationId
                );

        return customizeAuthorizationRequest(
                authorizationRequest
        );
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest
    ) {
        if (authorizationRequest == null) {
            return null;
        }

        Map<String, Object> additionalParameters =
                new HashMap<>(
                        authorizationRequest
                                .getAdditionalParameters()
                );

        additionalParameters.put(
                "prompt",
                "select_account"
        );

        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}