package pl.commercelink.starter.security.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.commercelink.starter.security.model.CustomUser;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String CUSTOM_PREFIX = "custom:";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, String> customAttributes = new HashMap<>();

        attributes.forEach((key, value) -> {
            if (key != null && key.startsWith(CUSTOM_PREFIX) && value != null) {
                String newKey = key.substring(CUSTOM_PREFIX.length());
                customAttributes.put(newKey, value.toString());
            }
        });

        return new CustomUser(oAuth2User, userRequest.getAccessToken(), customAttributes);
    }
}