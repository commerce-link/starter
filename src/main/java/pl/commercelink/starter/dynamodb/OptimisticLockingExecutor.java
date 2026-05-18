package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import pl.commercelink.starter.autoconfigure.OptimisticLockingProperties;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class OptimisticLockingExecutor {

    private final OptimisticLockingProperties properties;

    public OptimisticLockingExecutor(OptimisticLockingProperties properties) {
        this.properties = properties;
    }

    @Retryable(
            retryFor = ConditionalCheckFailedException.class,
            maxAttemptsExpression = "#{@optimisticLockingProperties.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@optimisticLockingProperties.delay}",
                    multiplierExpression = "#{@optimisticLockingProperties.multiplier}",
                    maxDelayExpression = "#{@optimisticLockingProperties.maxDelay}",
                    randomExpression = "#{@optimisticLockingProperties.random}"
            )
    )
    public <T> T modifyAndSave(Supplier<T> loader, Consumer<T> mutator, Consumer<T> saver) {
        T entity = loader.get();
        mutator.accept(entity);
        saver.accept(entity);
        return entity;
    }

    @Retryable(
            retryFor = ConditionalCheckFailedException.class,
            maxAttemptsExpression = "#{@optimisticLockingProperties.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@optimisticLockingProperties.delay}",
                    multiplierExpression = "#{@optimisticLockingProperties.multiplier}",
                    maxDelayExpression = "#{@optimisticLockingProperties.maxDelay}",
                    randomExpression = "#{@optimisticLockingProperties.random}"
            )
    )
    public <T, R> R modifyAndSaveReturning(Supplier<T> loader, Function<T, R> mutator, Consumer<T> saver) {
        T entity = loader.get();
        R result = mutator.apply(entity);
        saver.accept(entity);
        return result;
    }

    @Recover
    public <T> T recoverModifyAndSave(ConditionalCheckFailedException e,
                                      Supplier<T> loader, Consumer<T> mutator, Consumer<T> saver) {
        throw new OptimisticLockingExhaustedException(properties.getMaxAttempts(), e);
    }

    @Recover
    public <T, R> R recoverModifyAndSaveReturning(ConditionalCheckFailedException e,
                                                  Supplier<T> loader, Function<T, R> mutator, Consumer<T> saver) {
        throw new OptimisticLockingExhaustedException(properties.getMaxAttempts(), e);
    }
}
