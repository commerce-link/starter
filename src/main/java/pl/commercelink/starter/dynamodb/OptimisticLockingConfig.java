package pl.commercelink.starter.dynamodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class OptimisticLockingConfig {
}
