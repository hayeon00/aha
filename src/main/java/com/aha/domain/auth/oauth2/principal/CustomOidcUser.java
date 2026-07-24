package com.aha.domain.auth.oauth2.principal;

import com.aha.domain.user.entity.User;
import com.aha.domain.user.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOidcUser implements OidcUser, AhaOAuth2Principal {

    private final Long userId;
    private final String email;
    private final UserRole role;

    private final OidcUser oidcUser;

    public CustomOidcUser(
            User user,
            OidcUser oidcUser
    ) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.oidcUser = oidcUser;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
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
        return oidcUser.getName();
    }
}