package pl.commercelink.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

import java.net.URI;

@Configuration
public class SesConfig {

    @Value("${amazon.aws.region}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "prod", matchIfMissing = false)
    public SesV2Client sesProdClient() {
        return SesV2Client.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "localhost", matchIfMissing = true)
    public SesV2Client sesLocalClient(@Value("${localstack.aws.endpoint}") String endpoint) {
        return SesV2Client.builder()
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
