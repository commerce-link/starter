package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DynamoDbRepository<T> {

    protected final AmazonDynamoDB amazonDynamoDB;
    protected final DynamoDBMapper dynamoDBMapper;

    public DynamoDbRepository(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
    }

    public void save(T entity) {
        dynamoDBMapper.save(entity);
    }

    public void batchSave(List<T> entities) {
        dynamoDBMapper.batchSave(entities);
    }

    public void delete(T entity) {
        if (entity instanceof DeletionProtection) {
            DeletionProtection protectedEntity = (DeletionProtection) entity;
            if (protectedEntity.isDeletionProtection()) {
                throw new RuntimeException("Deletion protection is enabled for this instance thus it cannot be removed.");
            }
        }
        dynamoDBMapper.delete(entity);
    }

    public void delete(List<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    protected List<T> query(QueryRequest queryRequest, Class<T> clazz) {
        QueryResult queryResult = amazonDynamoDB.query(queryRequest);

        return queryResult.getItems().stream()
                .map(item -> dynamoDBMapper.marshallIntoObject(clazz, item))
                .collect(Collectors.toList());
    }

    protected void appendFilter(StringBuilder sb, String condition) {
        if (sb.length() > 0) {
            sb.append(" and ");
        }
        sb.append(condition);
    }

    protected List<T> queryWithPagination(DynamoDBQueryExpression<T> queryExpression, int page, int pageSize, Class<T> clazz) {
        PaginatedQueryList<T> queryList = dynamoDBMapper.query(clazz, queryExpression);
        return paginate(queryList, page, pageSize);
    }

    protected List<T> scanWithPagination(DynamoDBScanExpression scanExpression, int page, int pageSize, Class<T> clazz) {
        PaginatedScanList<T> scanList = dynamoDBMapper.scan(clazz, scanExpression);
        return paginate(scanList, page, pageSize);
    }

    private List<T> paginate(List<T> fullList, int page, int pageSize) {
        List<T> result = new ArrayList<>(pageSize + 1); // Fetch one extra item to check if there is a next page
        int fromIndex = Math.max((page - 1) * pageSize, 0);
        int toIndex = fromIndex + pageSize + 1;
        int index = 0;

        for (T entry : fullList) {
            if (index >= fromIndex && index < toIndex) {
                result.add(entry);
            }
            if (index >= toIndex) break;
            index++;
        }

        return result;
    }

}