package com.aha.domain.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.cookie")
public class AuthCookieProperties {

    private boolean secure;
    private long refreshTokenMaxAgeSeconds = 1209600;
    private boolean httpOnly = true;
    private String sameSite = "Lax";
    private String path = "/";
}