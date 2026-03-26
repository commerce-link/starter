package pl.commercelink.starter.secrets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.time.Instant;
import java.util.Optional;

@Service
public class ParameterStore {

    private final SsmClient ssmClient;
    private final ObjectMapper objectMapper = createObjectMapper();

    @Autowired
    public ParameterStore(SsmClient ssmClient) {
        this.ssmClient = ssmClient;
    }

    public <T> Optional<T> getParameter(String key, String name, String type, Class<T> clazz) {
        String parameterName = createParamName(key, name, type);

        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            String parameterValue = response.parameter().value();
            return Optional.ofNullable(fromJson(parameterValue, clazz));
        } catch (ParameterNotFoundException e) {
            return Optional.empty();
        }
    }

    public void putParameter(String key, String appName, String paramName, Object paramValue) {
        String parameterName = createParamName(key, appName, paramName);

        try {
            PutParameterRequest request = PutParameterRequest.builder()
                    .name(parameterName)
                    .value(toJson(paramValue))
                    .type(ParameterType.SECURE_STRING)
                    .overwrite(true)
                    .description(paramName + " parameter for app " + appName + " in key " + key)
                    .build();

            ssmClient.putParameter(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store " + paramName +
                    " parameter for app " + appName + " in key: " + key, e);
        }
    }

    public Optional<OAuth2AccessToken> getValidToken(String key, String name) {
        String parameterName = createParamName(key, name);

        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            String tokenJson = response.parameter().value();
            TokenData tokenData = fromJson(tokenJson, TokenData.class);

            if (tokenData.isExpired()) {
                return Optional.empty();
            }

            return Optional.of(
                new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    tokenData.getAccessToken(),
                    tokenData.getIssuedAt(),
                    tokenData.getExpiresAt()
            ));
        } catch (ParameterNotFoundException e) {
            return Optional.empty();
        }
    }

    public void storeToken(String key, String name,
                           OAuth2AccessToken token, String refreshToken) {
        String parameterName = createParamName(key, name);

        TokenData tokenData = new TokenData(
                token.getTokenValue(),
                refreshToken,
                token.getIssuedAt(),
                token.getExpiresAt(),
                token.getTokenType().getValue()
        );

        String tokenJson = toJson(tokenData);

        try {
            PutParameterRequest request = PutParameterRequest.builder()
                    .name(parameterName)
                    .value(tokenJson)
                    .type(ParameterType.SECURE_STRING)
                    .overwrite(true)
                    .description(name + " OAuth2 token for key " + key)
                    .build();

            ssmClient.putParameter(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store " + name +
                    " token for key: " + key, e);
        }
    }

    public void deleteParameter(String key, String name) {
        String parameterName = createParamName(key, name);
        try {
            ssmClient.deleteParameter(DeleteParameterRequest.builder().name(parameterName).build());
        } catch (ParameterNotFoundException e) {
            // already deleted
        }
    }

    public void deleteParameter(String key, String name, String type) {
        String parameterName = createParamName(key, name, type);
        try {
            ssmClient.deleteParameter(DeleteParameterRequest.builder().name(parameterName).build());
        } catch (ParameterNotFoundException e) {
            // already deleted
        }
    }

    private String createParamName(String key, String name) {
        return "/" + key + "/" + name;
    }

    private String createParamName(String key, String name, String type) {
        return "/" + key + "/" + name.toLowerCase() + "/" + type.toLowerCase();
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON secret", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize secret", e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        return objectMapper;
    }

    public static class TokenData {
        private String accessToken;
        private String refreshToken;
        private Instant issuedAt;
        private Instant expiresAt;
        private String tokenType;

        public TokenData() {
        }

        public TokenData(String accessToken, String refreshToken, Instant issuedAt,
                         Instant expiresAt, String tokenType) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.tokenType = tokenType;
        }

        @JsonIgnore
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt.minusSeconds(60)); // 1 minute buffer
        }

        // Getters and setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public Instant getIssuedAt() {
            return issuedAt;
        }

        public void setIssuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }
}
