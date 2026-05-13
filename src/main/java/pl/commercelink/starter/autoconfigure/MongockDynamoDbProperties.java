package pl.commercelink.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commercelink.mongock.dynamodb")
public class MongockDynamoDbProperties {

    private long readCapacityUnits = 50L;
    private long writeCapacityUnits = 50L;

    public long getReadCapacityUnits() {
        return readCapacityUnits;
    }

    public void setReadCapacityUnits(long readCapacityUnits) {
        this.readCapacityUnits = readCapacityUnits;
    }

    public long getWriteCapacityUnits() {
        return writeCapacityUnits;
    }

    public void setWriteCapacityUnits(long writeCapacityUnits) {
        this.writeCapacityUnits = writeCapacityUnits;
    }
}
