package pl.commercelink.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;

import java.net.URI;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Value("${amazon.aws.region}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "prod", matchIfMissing = false)
    public SchedulerClient prodSchedulerClient() {
        return SchedulerClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "localhost", matchIfMissing = true)
    public SchedulerClient localSchedulerClient(@Value("${localstack.aws.endpoint}") String endpoint) {
        return SchedulerClient.builder()
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
