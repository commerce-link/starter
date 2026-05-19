package pl.commercelink.starter.autoconfigure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptimisticLockingProperties {

    private int maxAttempts = 3;
    private long delay = 50L;
    private double multiplier = 2.0;
    private long maxDelay = 200L;
    private boolean random = true;
}
