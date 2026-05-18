package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import pl.commercelink.starter.autoconfigure.OptimisticLockingProperties;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        OptimisticLockingExecutorTest.TestConfig.class,
        OptimisticLockingExecutor.class
})
class OptimisticLockingExecutorTest {

    @Configuration
    @EnableRetry
    static class TestConfig {
        @Bean
        OptimisticLockingProperties optimisticLockingProperties() {
            return new OptimisticLockingProperties();
        }
    }


    @Autowired
    private OptimisticLockingExecutor executor;

    @Autowired
    private OptimisticLockingProperties properties;

    @Test
    @DisplayName("modifyAndSave succeeds on first attempt when saver does not throw ConditionalCheckFailedException")
    void modifyAndSaveSucceedsOnFirstAttemptWhenNoConditionalCheckFailedException() {
        // given
        AtomicInteger attempts = new AtomicInteger();

        // when
        String result = executor.modifyAndSave(
                () -> "entity",
                e -> {},
                e -> attempts.incrementAndGet()
        );

        // then
        assertThat(result).isEqualTo("entity");
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("modifyAndSave retries up to max attempts when ConditionalCheckFailedException is raised then eventually succeeds")
    void modifyAndSaveRetriesUpToMaxAttemptsWhenConditionalCheckFailedExceptionIsRaised() {
        // given
        AtomicInteger attempts = new AtomicInteger();

        // when
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

        // then
        assertThat(attempts.get()).isEqualTo(properties.getMaxAttempts());
    }

    @Test
    @DisplayName("modifyAndSave throws OptimisticLockingExhaustedException after max attempts exceeded with all attempts failing")
    void modifyAndSaveThrowsOptimisticLockingExhaustedExceptionAfterMaxAttemptsExceeded() {
        // given
        AtomicInteger attempts = new AtomicInteger();
        ConditionalCheckFailedException simulated = new ConditionalCheckFailedException("always conflicts");

        // when / then
        assertThatThrownBy(() -> executor.modifyAndSave(
                () -> "entity",
                e -> {},
                e -> {
                    attempts.incrementAndGet();
                    throw simulated;
                }
        ))
                .isInstanceOf(OptimisticLockingExhaustedException.class)
                .satisfies(ex -> {
                    OptimisticLockingExhaustedException exhausted = (OptimisticLockingExhaustedException) ex;
                    assertThat(exhausted.getAttempts()).isEqualTo(properties.getMaxAttempts());
                    assertThat(exhausted.getCause()).isInstanceOf(ConditionalCheckFailedException.class);
                });

        assertThat(attempts.get()).isEqualTo(properties.getMaxAttempts());
    }

    @Test
    @DisplayName("modifyAndSaveReturning retries on ConditionalCheckFailedException and returns mutator result on eventual success")
    void modifyAndSaveReturningRetriesAndReturnsResultFromMutatorOnEventualSuccess() {
        // given
        AtomicInteger attempts = new AtomicInteger();

        // when
        Integer result = executor.modifyAndSaveReturning(
                () -> "entity",
                e -> 42,
                e -> {
                    if (attempts.incrementAndGet() < 2) {
                        throw new ConditionalCheckFailedException("first attempt fails");
                    }
                }
        );

        // then
        assertThat(result).isEqualTo(42);
        assertThat(attempts.get()).isEqualTo(2);
    }
}
