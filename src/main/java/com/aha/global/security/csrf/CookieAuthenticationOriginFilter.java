package com.aha.global.security.csrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CookieAuthenticationOriginFilter
        extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS =
            Set.of(
                    "/api/v1/auth/reissue",
                    "/api/v1/auth/logout",
                    "/api/v1/auth/oauth/exchange"
            );

    private final Set<String> allowedOrigins;

    public CookieAuthenticationOriginFilter(
            @Value("${app.cors.allowed-origins}")
            String allowedOrigins
    ) {
        this.allowedOrigins =
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .map(this::normalizeOrigin)
                        .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        if (HttpMethod.OPTIONS.matches(
                request.getMethod()
        )) {
            return true;
        }

        if (!HttpMethod.POST.matches(
                request.getMethod()
        )) {
            return true;
        }

        return !PROTECTED_PATHS.contains(
                request.getRequestURI()
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestOrigin =
                resolveRequestOrigin(request);

        if (requestOrigin == null) {
            writeForbiddenResponse(response);
            return;
        }

        String normalizedRequestOrigin =
                normalizeOrigin(requestOrigin);

        if (!allowedOrigins.contains(
                normalizedRequestOrigin
        )) {
            writeForbiddenResponse(response);
            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private String resolveRequestOrigin(
            HttpServletRequest request
    ) {
        String origin =
                request.getHeader(
                        HttpHeaders.ORIGIN
                );

        if (origin != null
                && !origin.isBlank()
                && !"null".equalsIgnoreCase(origin)) {
            return origin;
        }

        String referer =
                request.getHeader(
                        HttpHeaders.REFERER
                );

        if (referer == null
                || referer.isBlank()) {
            return null;
        }

        try {
            URI refererUri =
                    URI.create(referer);

            String scheme =
                    refererUri.getScheme();

            String host =
                    refererUri.getHost();

            if (scheme == null
                    || host == null) {
                return null;
            }

            int port =
                    refererUri.getPort();

            if (port == -1) {
                return scheme
                        + "://"
                        + host;
            }

            return scheme
                    + "://"
                    + host
                    + ":"
                    + port;

        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalizeOrigin(
            String origin
    ) {
        String normalized =
                origin.trim();

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return normalized;
    }

    private void writeForbiddenResponse(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write(
                """
                {
                  "status": 403,
                  "message": "허용되지 않은 출처의 요청입니다.",
                  "data": null
                }
                """
        );
    }
}