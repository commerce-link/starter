package pl.commercelink.starter.dynamodb;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import pl.commercelink.starter.autoconfigure.OptimisticLockingProperties;

@Configuration
@EnableRetry
@EnableConfigurationProperties(OptimisticLockingProperties.class)
public class OptimisticLockingConfig {
}
