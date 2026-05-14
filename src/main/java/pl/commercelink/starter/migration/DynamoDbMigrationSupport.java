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
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared helpers for DynamoDB schema changes (e.g. Mongock change units) using AWS
 * SDK for Java v1 ({@link AmazonDynamoDB}).
 *
 <p>
 * {@link #createTableIfAbsent(AmazonDynamoDB, CreateTableRequest)} is
 * idempotent: if the table
 * already exists, the call completes without error.
 */
public final class DynamoDbMigrationSupport {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbMigrationSupport.class);

    private DynamoDbMigrationSupport() {
    }

    /**
     * Creates the table described by {@code request} if it does not already exist.
     * Safe to call
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

    /**
     * Scans the whole table and runs {@link #executeUpdate} for each item using
     * only the given key attributes.
     */
    public static void backfillAttributes(AmazonDynamoDB dynamoDb, String tableName,
            List<String> keyNames,
            String updateExpression,
            Map<String, String> expressionAttributeNames,
            Map<String, AttributeValue> expressionAttributeValues) {

        scanAndProcess(dynamoDb, tableName, keyNames, key -> executeUpdate(dynamoDb, tableName, key, updateExpression,
                expressionAttributeNames, expressionAttributeValues));
    }

    /**
     * Paginated table scan projecting only {@code keyNames}; each scanned item is
     * reduced to the key map and passed to {@code itemProcessor}.
     */
    public static void scanAndProcess(AmazonDynamoDB dynamoDb, String tableName, List<String> keyNames,
            Consumer<Map<String, AttributeValue>> itemProcessor) {
        Map<String, AttributeValue> exclusiveStartKey = null;
        String projectionExpression = String.join(", ", keyNames);

        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(tableName)
                    .withProjectionExpression(projectionExpression);

            if (hasMorePages(exclusiveStartKey)) {
                scanRequest.withExclusiveStartKey(exclusiveStartKey);
            }

            ScanResult result = dynamoDb.scan(scanRequest);

            for (Map<String, AttributeValue> item : result.getItems()) {
                itemProcessor.accept(extractKey(item, keyNames));
            }

            exclusiveStartKey = result.getLastEvaluatedKey();
        } while (hasMorePages(exclusiveStartKey));
    }

    /**
     * Runs {@code UpdateItem} for the given primary key with optional expression
     * name/value maps.
     */
    public static void executeUpdate(AmazonDynamoDB dynamoDb, String tableName,
            Map<String, AttributeValue> key,
            String updateExpression,
            Map<String, String> expressionAttributeNames,
            Map<String, AttributeValue> expressionAttributeValues) {

        UpdateItemRequest updateRequest = new UpdateItemRequest()
                .withTableName(tableName)
                .withKey(key)
                .withUpdateExpression(updateExpression);

        if (expressionAttributeNames != null && !expressionAttributeNames.isEmpty()) {
            updateRequest.withExpressionAttributeNames(expressionAttributeNames);
        }
        if (expressionAttributeValues != null && !expressionAttributeValues.isEmpty()) {
            updateRequest.withExpressionAttributeValues(expressionAttributeValues);
        }

        dynamoDb.updateItem(updateRequest);
    }

    private static Map<String, AttributeValue> extractKey(Map<String, AttributeValue> item, List<String> keyNames) {
        return keyNames.stream()
                .filter(item::containsKey)
                .collect(Collectors.toMap(k -> k, item::get));
    }

    private static boolean hasMorePages(Map<String, AttributeValue> lastEvaluatedKey) {
        return lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty();
    }

}
