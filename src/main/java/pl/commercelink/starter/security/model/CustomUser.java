package pl.commercelink.starter.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record CustomUser(OAuth2User oAuth2User, OAuth2AccessToken accessToken,
                         Map<String, String> customAttributes) implements OAuth2User, OidcUser {

    public CustomUser(OAuth2User oAuth2User, OAuth2AccessToken accessToken, Map<String, String> customAttributes) {
        this.oAuth2User = oAuth2User;
        this.accessToken = accessToken;
        this.customAttributes = new HashMap<>(customAttributes);
    }

    public Optional<String> getCustomAttribute(String key) {
        return Optional.ofNullable(customAttributes.get(key));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Object role = getCustomAttribute("role").orElse(null);
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }

    @Override
    public Map<String, Object> getClaims() {
        return ((DefaultOidcUser) oAuth2User).getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return ((DefaultOidcUser) oAuth2User).getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return ((DefaultOidcUser) oAuth2User).getIdToken();
    }
}