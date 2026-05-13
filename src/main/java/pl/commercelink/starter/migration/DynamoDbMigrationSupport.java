package pl.commercelink.starter.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared helpers for DynamoDB schema changes (e.g. Mongock change units) using AWS
 * SDK for Java v1 ({@link AmazonDynamoDB}).
 *
 * <p>{@link #createTableIfAbsent(AmazonDynamoDB, CreateTableRequest)} is idempotent: if the table
 * already exists, the call completes without error.
 */
public final class DynamoDbMigrationSupport {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbMigrationSupport.class);

    private DynamoDbMigrationSupport() {}

    /**
     * Creates the table described by {@code request} if it does not already exist. Safe to call
     * repeatedly for the same table name.
     */
    public static void createTableIfAbsent(AmazonDynamoDB dynamoDb, CreateTableRequest request) {
        try {
            dynamoDb.createTable(request);
            log.info("Created DynamoDB table: {}", request.getTableName());
        } catch (ResourceInUseException e) {
            log.info("DynamoDB table already exists, skipping: {}", request.getTableName());
        }
    }

    /** Attribute definition with {@link ScalarAttributeType#S}. */
    public static AttributeDefinition stringAttribute(String name) {
        return new AttributeDefinition(name, ScalarAttributeType.S);
    }

    /** Partition (hash) key in the key schema. */
    public static KeySchemaElement hashKey(String name) {
        return new KeySchemaElement(name, KeyType.HASH);
    }

    /** Sort (range) key in the key schema. */
    public static KeySchemaElement rangeKey(String name) {
        return new KeySchemaElement(name, KeyType.RANGE);
    }

    /** GSI/LSI projection of all table attributes. */
    public static Projection allProjection() {
        return new Projection().withProjectionType(ProjectionType.ALL);
    }

    /** GSI/LSI projection including only the given non-key attributes. */
    public static Projection includeProjection(String... nonKeyAttributes) {
        List<String> attrs = Arrays.asList(nonKeyAttributes);
        return new Projection()
                .withProjectionType(ProjectionType.INCLUDE)
                .withNonKeyAttributes(attrs);
    }
}
