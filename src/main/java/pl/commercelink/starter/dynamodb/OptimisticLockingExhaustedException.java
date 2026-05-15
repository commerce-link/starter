package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

public class OptimisticLockingExhaustedException extends RuntimeException {

    private final int attempts;

    public OptimisticLockingExhaustedException(int attempts, ConditionalCheckFailedException cause) {
        super("Optimistic locking retry exhausted after " + attempts + " attempts", cause);
        this.attempts = attempts;
    }

    public int getAttempts() {
        return attempts;
    }
}
