package pl.commercelink.starter.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import pl.commercelink.starter.secrets.SecretsManager;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST;

@Configuration
public class CognitoOAuthConfig {

    @Value("${application.env}")
    private String env;

    @Value("${spring.security.oauth2.client.registration.cognito.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.provider.cognito.issuer-uri}")
    private String issuerUri;

    @Value("${cognito.domain}")
    private String cognitoDomain;

    @Value("${spring.security.oauth2.client.registration.cognito.redirect-uri}")
    private String redirectUri;

    @Value("${cognito.client-secret-name:cognito-client-secret}")
    private String clientSecretName;

    @Autowired
    private SecretsManager secretsManager;

    @Autowired
    private Environment environmentProperty;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration cognitoRegistration = ClientRegistration.withRegistrationId("cognito")
                .clientId(clientId)
                .clientSecret(getClientSecret())
                .clientAuthenticationMethod(CLIENT_SECRET_POST)
                .authorizationUri(cognitoDomain + "/oauth2/authorize")
                .tokenUri(cognitoDomain + "/oauth2/token")
                .userInfoUri(cognitoDomain + "/oauth2/userInfo")
                .jwkSetUri(issuerUri + "/.well-known/jwks.json")
                .userNameAttributeName("name")
                .clientName("Cognito")
                .authorizationGrantType(AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .build();

        return new InMemoryClientRegistrationRepository(cognitoRegistration);
    }

    private String getClientSecret() {
        if (env.equals("prod")) {
            return secretsManager.getSecret(clientSecretName);
        }
        return environmentProperty.getProperty("spring.security.oauth2.client.registration.cognito.client-secret");
    }
}
