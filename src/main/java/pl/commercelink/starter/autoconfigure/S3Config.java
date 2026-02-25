package pl.commercelink.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${amazon.aws.region}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "prod", matchIfMissing = false)
    public S3Client prodS3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .httpClientBuilder(
                        ApacheHttpClient.builder().maxConnections(100)
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "localhost", matchIfMissing = true)
    public S3Client localS3Client(@Value("${localstack.aws.endpoint}") String endpoint) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .httpClientBuilder(
                        ApacheHttpClient.builder().maxConnections(100)
                )
                .build();
    }
}
