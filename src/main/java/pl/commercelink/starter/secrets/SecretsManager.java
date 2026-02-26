package pl.commercelink.starter.secrets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

@Component
public class SecretsManager {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper = createObjectMapper();

    public SecretsManager(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public String getSecret(String secretName) {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get secret value for secret: " + secretName, e);
        }

        return getSecretValueResponse.secretString();
    }

    public void createSecret(String secretName, String secretValue) {
        CreateSecretRequest createSecretRequest = CreateSecretRequest.builder()
                .name(secretName)
                .secretString(secretValue)
                .build();

        try {
            secretsManagerClient.createSecret(createSecretRequest);
        } catch (ResourceExistsException e) {
            throw new RuntimeException("Secret already exists: ", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create secret: ", e);
        }
    }

    public void updateSecret(String secretName, String secretValue) {
        UpdateSecretRequest updateSecretRequest = UpdateSecretRequest.builder()
                .secretId(secretName)
                .secretString(secretValue)
                .build();

        try {
            secretsManagerClient.updateSecret(updateSecretRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update secret: ", e);
        }
    }

    public <T> void createSecret(String secretName, T secretValue) {
        createSecret(secretName, toJson(secretValue));
    }

    public <T> void updateSecret(String secretName, T newSecretValue) {
        updateSecret(secretName, toJson(newSecretValue));
    }

    public <T> T getSecret(String secretName, Class<T> valueType) {
        return fromJson(getSecret(secretName), valueType);
    }

    public boolean exists(String secretName){
        try {
            getSecret(secretName);
            return true;
        } catch (RuntimeException e){
            return false;
        }
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
}