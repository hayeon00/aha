package com.aha.global.config;

import com.aha.domain.auth.oauth2.handler.OAuth2LoginFailureHandler;
import com.aha.domain.auth.oauth2.handler.OAuth2LoginSuccessHandler;
import com.aha.domain.auth.oauth2.resolver.CustomOAuth2AuthorizationRequestResolver;
import com.aha.domain.auth.oauth2.service.CustomOAuth2UserService;
import com.aha.domain.auth.oauth2.service.CustomOidcUserService;
import com.aha.global.security.csrf.CookieAuthenticationOriginFilter;
import com.aha.global.security.jwt.JwtAuthenticationFilter;
import com.aha.global.security.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final CookieAuthenticationOriginFilter
            cookieAuthenticationOriginFilter;

    private final CustomOAuth2UserService
            customOAuth2UserService;

    private final CustomOidcUserService
            customOidcUserService;

    private final OAuth2LoginSuccessHandler
            oauth2LoginSuccessHandler;

    private final OAuth2LoginFailureHandler
            oauth2LoginFailureHandler;

    private final CustomOAuth2AuthorizationRequestResolver
            customOAuth2AuthorizationRequestResolver;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .securityContext(securityContext ->
                        securityContext
                                .securityContextRepository(
                                        new RequestAttributeSecurityContextRepository()
                                )
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (
                                        request,
                                        response,
                                        authException
                                ) -> {
                                    response.setStatus(
                                            HttpServletResponse
                                                    .SC_UNAUTHORIZED
                                    );

                                    response.setContentType(
                                            "application/json;charset=UTF-8"
                                    );

                                    response.getWriter().write(
                                            """
                                            {
                                              "status": 401,
                                              "message": "인증이 필요합니다.",
                                              "data": null
                                            }
                                            """
                                    );
                                }
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue",
                                "/api/v1/auth/oauth/exchange",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/v1/exams",
                                "/api/v1/exam-versions/*/syllabus",
                                "/uploads/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/admin/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/users/**"
                        )
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(
                                        customOAuth2AuthorizationRequestResolver
                                )
                        )

                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(
                                        customOAuth2UserService
                                )
                                .oidcUserService(
                                        customOidcUserService
                                )
                        )

                        .successHandler(
                                oauth2LoginSuccessHandler
                        )

                        .failureHandler(
                                oauth2LoginFailureHandler
                        )
                )

                /*
                 * 쿠키 기반 인증 요청을 JWT 인증 처리보다 먼저 검사합니다.
                 */
                .addFilterBefore(
                        cookieAuthenticationOriginFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                parseAllowedOrigins()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin"
                )
        );

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    private List<String> parseAllowedOrigins() {
        return Arrays.stream(
                        allowedOrigins.split(",")
                )
                .map(String::trim)
                .filter(origin ->
                        !origin.isBlank()
                )
                .toList();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}