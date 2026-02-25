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
import java.util.List;
import java.util.Map;

public class CustomUser implements OAuth2User, OidcUser {

    private final OAuth2User oAuth2User;
    private final String storeId;
    private final OAuth2AccessToken accessToken;

    public CustomUser(OAuth2User oAuth2User, OAuth2AccessToken accessToken, String storeId) {
        this.oAuth2User = oAuth2User;
        this.accessToken = accessToken;
        this.storeId = storeId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }

    public String getStoreId() {
        return storeId;
    }

    public OAuth2AccessToken getAccessToken() {
        return accessToken;
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