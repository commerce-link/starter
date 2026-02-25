package pl.commercelink.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.net.URI;

@Configuration
class SecretsManagerConfig {

    @Value("${amazon.aws.region}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "prod")
    public SecretsManagerClient prodSecretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "localhost")
    public SecretsManagerClient localSecretsManagerClient(@Value("${localstack.aws.endpoint}") String endpoint) {
        return SecretsManagerClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }
}

