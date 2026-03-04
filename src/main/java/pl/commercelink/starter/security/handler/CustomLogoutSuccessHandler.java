package pl.commercelink.starter.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pl.commercelink.starter.secrets.SecretsManager;
import pl.commercelink.starter.security.model.CustomUser;

import java.io.IOException;

import static java.util.Base64.getEncoder;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${application.env}")
    private String env;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    @Value("${cognito.domain}")
    private String cognitoDomain;

    @Value("${cognito.logout.redirect-uri:}")
    private String logoutRedirectUri;

    private final OAuth2AuthorizedClientService authorizedClientService;

    private final SecretsManager secretsManager;

    private final Environment environmentProperty;

    public CustomLogoutSuccessHandler(OAuth2AuthorizedClientService authorizedClientService, SecretsManager secretsManager, Environment environmentProperty) {
        this.authorizedClientService = authorizedClientService;
        this.secretsManager = secretsManager;
        this.environmentProperty = environmentProperty;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUser) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    "cognito", authentication.getName());

            OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
            if (StringUtils.isNotBlank(refreshToken.getTokenValue())) {
                revokeRefreshToken(refreshToken.getTokenValue());
            }

            String cognitoLogoutUrl = cognitoDomain + "/logout"
                    + "?client_id=" + clientId;
            if (StringUtils.isNotBlank(logoutRedirectUri)) {
                cognitoLogoutUrl += "&logout_uri=" + logoutRedirectUri;
            }

            response.sendRedirect(cognitoLogoutUrl);
        }
    }

    private void revokeRefreshToken(String refreshToken) {
        String revokeUrl = cognitoDomain + "/oauth2/revoke";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String authHeader = "Basic " + getEncoder().encodeToString((clientId + ":" + getClientSecret()).getBytes());
        headers.set("Authorization", authHeader);
        String body = "token=" + refreshToken + "&client_id=" + clientId;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(revokeUrl, request, String.class);
    }

    private String getClientSecret() {
        if (env.equals("prod")) {
            return secretsManager.getSecret("cognito-client-secret");
        }
        return environmentProperty.getProperty("spring.security.oauth2.client.registration.cognito.client-secret");
    }
}
