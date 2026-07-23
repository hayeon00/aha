package com.aha.domain.auth.oauth2.handler;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler
        implements AuthenticationFailureHandler {

    private static final String DEFAULT_ERROR_CODE =
            "OAUTH_LOGIN_FAILED";

    @Value("${app.frontend.oauth-callback-url}")
    private String oauthCallbackUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String errorCode =
                resolveErrorCode(exception);

        log.warn(
                "OAuth 로그인 실패. errorCode={}, exceptionType={}",
                errorCode,
                exception.getClass().getSimpleName()
        );

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(oauthCallbackUrl)
                        .queryParam(
                                "error",
                                errorCode
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String resolveErrorCode(
            AuthenticationException exception
    ) {
        BusinessException businessException =
                findCause(
                        exception,
                        BusinessException.class
                );

        if (businessException != null) {
            return mapBusinessErrorCode(
                    businessException.getErrorCode()
            );
        }

        if (exception
                instanceof OAuth2AuthenticationException oauthException) {

            String providerErrorCode =
                    oauthException
                            .getError()
                            .getErrorCode();

            if ("access_denied".equals(
                    providerErrorCode
            )) {
                return "OAUTH_ACCESS_DENIED";
            }

            if ("authorization_request_not_found".equals(
                    providerErrorCode
            )) {
                return "OAUTH_SESSION_EXPIRED";
            }

            if ("invalid_state_parameter".equals(
                    providerErrorCode
            )) {
                return "OAUTH_INVALID_STATE";
            }
        }

        return DEFAULT_ERROR_CODE;
    }

    private String mapBusinessErrorCode(
            ErrorCode errorCode
    ) {
        return switch (errorCode) {
            case ACCOUNT_NOT_ACTIVE ->
                    "ACCOUNT_NOT_ACTIVE";

            case SOCIAL_ACCOUNT_LINK_REQUIRED ->
                    "SOCIAL_ACCOUNT_LINK_REQUIRED";

            case SOCIAL_EMAIL_REQUIRED ->
                    "SOCIAL_EMAIL_REQUIRED";

            default ->
                    DEFAULT_ERROR_CODE;
        };
    }

    private <T extends Throwable> T findCause(
            Throwable throwable,
            Class<T> targetType
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (targetType.isInstance(current)) {
                return targetType.cast(current);
            }

            current = current.getCause();
        }

        return null;
    }
}