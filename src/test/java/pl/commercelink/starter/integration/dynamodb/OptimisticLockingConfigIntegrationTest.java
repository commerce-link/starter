package pl.commercelink.starter.integration.dynamodb;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import pl.commercelink.starter.autoconfigure.OptimisticLockingProperties;
import pl.commercelink.starter.dynamodb.OptimisticLockingConfig;
import pl.commercelink.starter.dynamodb.OptimisticLockingExecutor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {
                OptimisticLockingConfig.class,
                OptimisticLockingExecutor.class
        },
        properties = {
                "commercelink.optimistic-locking.max-attempts=4",
                "commercelink.optimistic-locking.delay=1",
                "commercelink.optimistic-locking.max-delay=2",
                "commercelink.optimistic-locking.multiplier=1.0",
                "commercelink.optimistic-locking.random=false"
        }
)
public class OptimisticLockingConfigIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private OptimisticLockingExecutor executor;

    @Autowired
    private OptimisticLockingProperties properties;

    @Test
    @DisplayName("registers optimisticLockingProperties bean under the simple name the @Retryable SpEL expressions reference")
    void registersBeanUnderNameReferencedBySpel() {
        assertThat(context.containsBean("optimisticLockingProperties")).isTrue();
        assertThat(context.getBean("optimisticLockingProperties"))
                .isInstanceOf(OptimisticLockingProperties.class);
    }

    @Test
    @DisplayName("binds commercelink.optimistic-locking.* properties onto the registered bean after the @ConfigurationProperties move to @Bean method")
    void bindsPropertiesOntoRegisteredBean() {
        assertThat(properties.getMaxAttempts()).isEqualTo(4);
        assertThat(properties.getDelay()).isEqualTo(1L);
        assertThat(properties.getMaxDelay()).isEqualTo(2L);
        assertThat(properties.getMultiplier()).isEqualTo(1.0);
        assertThat(properties.isRandom()).isFalse();
    }

    @Test
    @DisplayName("@Retryable SpEL bean reference @optimisticLockingProperties resolves at runtime when modifyAndSave triggers retries via production wiring")
    void retryableSpelResolvesAgainstProductionWiring() {
        AtomicInteger attempts = new AtomicInteger();

        executor.modifyAndSave(
                () -> "entity",
                e -> {},
                e -> {
                    int attempt = attempts.incrementAndGet();
                    if (attempt < properties.getMaxAttempts()) {
                        throw new ConditionalCheckFailedException("simulated conflict on attempt " + attempt);
                    }
                }
        );

        assertThat(attempts.get()).isEqualTo(properties.getMaxAttempts());
    }
}
