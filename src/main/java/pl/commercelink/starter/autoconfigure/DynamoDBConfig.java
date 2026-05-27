package pl.commercelink.starter.autoconfigure;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDBConfig {

    @Value("${amazon.aws.region}")
    private String amazonAWSRegion;

    @Value("${application.env}")
    private String env;

    @Value("${amazon.aws.dynamodb.localEndpoint:http://localhost:8000}")
    private String dynamodbLocalEndpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder standard = AmazonDynamoDBClientBuilder.standard();
        if (env != null && env.equals("prod")) {
            standard.withRegion(amazonAWSRegion);
        } else {
            standard.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamodbLocalEndpoint, "eu-central-1"));
        }
        return standard.build();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }
}