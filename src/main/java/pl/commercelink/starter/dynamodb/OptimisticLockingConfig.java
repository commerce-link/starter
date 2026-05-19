package pl.commercelink.starter.dynamodb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import pl.commercelink.starter.autoconfigure.OptimisticLockingProperties;

@Configuration
@EnableRetry
@EnableConfigurationProperties
public class OptimisticLockingConfig {

    @Bean
    @ConfigurationProperties(prefix = "commercelink.optimistic-locking")
    public OptimisticLockingProperties optimisticLockingProperties() {
        return new OptimisticLockingProperties();
    }
}
