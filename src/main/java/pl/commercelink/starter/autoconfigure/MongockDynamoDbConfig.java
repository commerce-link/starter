package pl.commercelink.starter.autoconfigure;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import io.mongock.driver.api.driver.ConnectionDriver;
import io.mongock.driver.dynamodb.driver.DynamoDBDriver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MongockDynamoDbProperties.class)
public class MongockDynamoDbConfig {

    @Bean
    public ConnectionDriver mongockConnectionDriver(
            AmazonDynamoDB amazonDynamoDB,
            MongockDynamoDbProperties mongockDynamoDbProperties) {
        AmazonDynamoDBClient client = (AmazonDynamoDBClient) amazonDynamoDB;
        DynamoDBDriver driver = DynamoDBDriver.withDefaultLock(client);
        driver.setProvisionedThroughput(new ProvisionedThroughput(
                mongockDynamoDbProperties.getReadCapacityUnits(),
                mongockDynamoDbProperties.getWriteCapacityUnits()));
        return driver;
    }
}
