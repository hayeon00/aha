package com.aha.global.security.jwt;

import com.aha.global.security.CustomUserDetails;
import com.aha.global.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();

        return uri.equals("/api/v1/auth/signup")
                || uri.equals("/api/v1/auth/login")
                || uri.equals("/api/v1/auth/reissue")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/oauth2/")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtTokenProvider.validateAccessToken(token)) {
            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            response.setContentType(
                    "application/json;charset=UTF-8"
            );
            response.getWriter().write("""
                    {
                      "status": 401,
                      "message": "유효하지 않거나 만료된 토큰입니다.",
                      "data": null
                    }
                    """);
            return;
        }

        /*
         * Access Token의 subject에 저장된
         * Aha 내부 userId를 가져옵니다.
         */
        Long userId =
                jwtTokenProvider.getUserId(token);

        CustomUserDetails userDetails =
                customUserDetailsService
                        .loadUserById(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private String resolveToken(
            HttpServletRequest request
    ) {
        String authorization =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }
}