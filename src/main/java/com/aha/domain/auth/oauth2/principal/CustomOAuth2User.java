package com.aha.domain.auth.oauth2.principal;

import com.aha.domain.user.entity.User;
import com.aha.domain.user.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, AhaOAuth2Principal {

    private final Long userId;
    private final String email;
    private final UserRole role;

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public CustomOAuth2User(
            User user,
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + role.name()
                )
        );
    }

    @Override
    public String getName() {
        Object nameAttribute =
                attributes.get(nameAttributeKey);

        if (nameAttribute == null) {
            return String.valueOf(userId);
        }

        return String.valueOf(nameAttribute);
    }
}