package com.aha.domain.auth.oauth2.handler;

import com.aha.domain.auth.oauth2.principal.AhaOAuth2Principal;
import com.aha.domain.auth.oauth2.service.OAuthAuthorizationCodeService;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.repository.UserRepository;
import com.aha.domain.user.service.UserExamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String FRONTEND_CALLBACK_URL =
            "http://localhost:5173/oauth/callback";

    private final UserRepository userRepository;
    private final UserExamService userExamService;
    private final OAuthAuthorizationCodeService authorizationCodeService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        AhaOAuth2Principal principal =
                extractPrincipal(authentication);

        User user = userRepository.findById(
                        principal.getUserId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "소셜 로그인 사용자를 찾을 수 없습니다."
                        )
                );

        userExamService.syncSupportedExamsForUser(
                user.getId()
        );

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            throw new IllegalStateException(
                    "OAuth 로그인 세션을 찾을 수 없습니다."
            );
        }

        String authorizationCode =
                authorizationCodeService.createCode(
                        user,
                        session.getId()
                );

        SecurityContextHolder.clearContext();

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(
                                FRONTEND_CALLBACK_URL
                        )
                        .queryParam(
                                "code",
                                authorizationCode
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private AhaOAuth2Principal extractPrincipal(
            Authentication authentication
    ) {
        Object principal =
                authentication.getPrincipal();

        if (principal
                instanceof AhaOAuth2Principal ahaPrincipal) {
            return ahaPrincipal;
        }

        throw new IllegalStateException(
                "지원하지 않는 OAuth2 Principal입니다."
        );
    }


}