package com.aha.global.security.websocket;

import com.aha.global.security.CustomUserDetails;
import com.aha.global.security.CustomUserDetailsService;
import com.aha.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message,
            StompHeaderAccessor.class
        );

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader(
            HttpHeaders.AUTHORIZATION
        );

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("WebSocket access token is required.");
        }

        String accessToken = authorization.substring(BEARER_PREFIX.length());

        if (!jwtTokenProvider.validateAccessToken(accessToken)) {
            throw new BadCredentialsException("Invalid WebSocket access token.");
        }

        CustomUserDetails userDetails = customUserDetailsService.loadUserById(
            jwtTokenProvider.getUserId(accessToken)
        );

        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            throw new BadCredentialsException("Unavailable user account.");
        }

        accessor.setUser(
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            )
        );

        return message;
    }
}
