package pl.commercelink.starter.security.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.commercelink.starter.secrets.SecretsManager;
import pl.commercelink.starter.security.model.CustomUser;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.util.Base64.getEncoder;

@Component
public class CustomTokenRefreshFilter extends OncePerRequestFilter {

    @Value("${application.env}")
    private String env;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    @Value("${cognito.domain}")
    private String cognitoDomain;

    private final OAuth2AuthorizedClientService authorizedClientService;

    private final SecretsManager secretsManager;

    private final Environment environmentProperty;

    public CustomTokenRefreshFilter(OAuth2AuthorizedClientService authorizedClientService, SecretsManager secretsManager, Environment environmentProperty) {
        this.authorizedClientService = authorizedClientService;
        this.secretsManager = secretsManager;
        this.environmentProperty = environmentProperty;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUser) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    "cognito", authentication.getName());

            if (authorizedClient != null && isExpired(authorizedClient.getAccessToken())) {
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                if (refreshToken != null) {
                    OAuth2AccessTokenResponse tokenResponse = refreshAccessToken(refreshToken.getTokenValue());
                    Authentication updatedAuthentication = updateAuthInformation(authentication, tokenResponse.getAccessToken());

                    if (updatedAuthentication != null) {
                        OAuth2AuthorizedClient updatedClient = new OAuth2AuthorizedClient(
                                authorizedClient.getClientRegistration(),
                                updatedAuthentication.getName(),
                                tokenResponse.getAccessToken(),
                                authorizedClient.getRefreshToken());

                        authorizedClientService.saveAuthorizedClient(updatedClient, updatedAuthentication);
                        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private OAuth2AccessTokenResponse refreshAccessToken(String refreshToken) {
        String tokenUrl = cognitoDomain + "/oauth2/token";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String authHeader = "Basic " + getEncoder().encodeToString((clientId + ":" + getClientSecret()).getBytes());
        headers.set("Authorization", authHeader);

        String body = "grant_type=refresh_token"
                + "&client_id=" + clientId
                + "&refresh_token=" + refreshToken;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            TokenResponse tokenData = response.getBody();

            String accessToken = tokenData.getAccessToken();
            String idToken = tokenData.getIdToken();
            int expiresIn = tokenData.getExpiresIn();

            Map<String, Object> additionalParameters = new HashMap<>();
            additionalParameters.put("id_token", idToken);

            return OAuth2AccessTokenResponse.withToken(accessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(expiresIn)
                    .additionalParameters(additionalParameters)
                    .build();
        } else {
            throw new RuntimeException("Failed to refresh token: " + response.getStatusCode());
        }
    }

    private OAuth2AuthenticationToken updateAuthInformation(Authentication currentAuth, OAuth2AccessToken accessToken) {
        if (currentAuth instanceof OAuth2AuthenticationToken oauthToken) {
            if (oauthToken.getPrincipal() instanceof CustomUser oldUser) {
                CustomUser newUser = new CustomUser(
                        oldUser,
                        accessToken,
                        oldUser.customAttributes()
                );
                return new OAuth2AuthenticationToken(
                        newUser,
                        newUser.getAuthorities(),
                        oauthToken.getAuthorizedClientRegistrationId()
                );
            }
        }
        return null;
    }

    private boolean isExpired(OAuth2AccessToken token) {
        return token.getExpiresAt() != null && Instant.now().isAfter(token.getExpiresAt().minusSeconds(60));
    }

    private String getClientSecret() {
        if (env.equals("prod")) {
            return secretsManager.getSecret("cognito-client-secret");
        }
        return environmentProperty.getProperty("spring.security.oauth2.client.registration.cognito.client-secret");
    }

    static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("id_token")
        private String idToken;

        @JsonProperty("expires_in")
        private int expiresIn;

        @JsonProperty("token_type")
        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }
    }
}
