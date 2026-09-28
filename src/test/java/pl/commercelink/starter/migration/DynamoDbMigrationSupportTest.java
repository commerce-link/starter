package pl.commercelink.starter.migration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.util.TableUtils.TableNeverTransitionedToStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbMigrationSupportTest {

    private static final String TABLE = "Orders";
    private static final int TIMEOUT_MILLIS = 2_000;
    private static final int POLL_MILLIS = 10;

    @Mock
    AmazonDynamoDB dynamoDb;

    @Test
    void createTableIfAbsentReturnsOnlyOnceTheNewTableIsActive() {
        // given
        describeTableReturns(TableStatus.CREATING, TableStatus.ACTIVE);

        // when
        DynamoDbMigrationSupport.createTableIfAbsent(dynamoDb, request(), TIMEOUT_MILLIS, POLL_MILLIS);

        // then
        verify(dynamoDb).createTable(request());
        verify(dynamoDb, times(2)).describeTable(any(DescribeTableRequest.class));
    }

    @Test
    void createTableIfAbsentToleratesTableNotVisibleRightAfterCreation() {
        // given: DescribeTable is eventually consistent and may not see a table created a moment ago
        when(dynamoDb.describeTable(any(DescribeTableRequest.class)))
                .thenThrow(new ResourceNotFoundException("not yet visible"))
                .thenReturn(describeResult(TableStatus.ACTIVE));

        // when
        DynamoDbMigrationSupport.createTableIfAbsent(dynamoDb, request(), TIMEOUT_MILLIS, POLL_MILLIS);

        // then
        verify(dynamoDb, times(2)).describeTable(any(DescribeTableRequest.class));
    }

    @Test
    void createTableIfAbsentWaitsForAnAlreadyExistingTableToBecomeActive() {
        // given: another instance created the table a moment ago and it is still being created
        when(dynamoDb.createTable(any(CreateTableRequest.class))).thenThrow(new ResourceInUseException("exists"));
        describeTableReturns(TableStatus.CREATING, TableStatus.ACTIVE);

        // when
        DynamoDbMigrationSupport.createTableIfAbsent(dynamoDb, request(), TIMEOUT_MILLIS, POLL_MILLIS);

        // then
        verify(dynamoDb, times(2)).describeTable(any(DescribeTableRequest.class));
    }

    @Test
    void createTableIfAbsentFailsWhenTheTableNeverBecomesActive() {
        // given
        when(dynamoDb.describeTable(any(DescribeTableRequest.class))).thenReturn(describeResult(TableStatus.CREATING));

        // when / then
        assertThatThrownBy(() -> DynamoDbMigrationSupport.createTableIfAbsent(dynamoDb, request(), 50, POLL_MILLIS))
                .isInstanceOf(TableNeverTransitionedToStateException.class);
    }

    @Test
    void createTableIfAbsentDoesNotWaitWhenTheTableIsAlreadyActive() {
        // given
        describeTableReturns(TableStatus.ACTIVE);
        long start = System.nanoTime();

        // when: the default overload, whose poll interval is meant for real AWS, must not sleep here
        DynamoDbMigrationSupport.createTableIfAbsent(dynamoDb, request());

        // then
        assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(500);
        verify(dynamoDb, times(1)).describeTable(any(DescribeTableRequest.class));
    }

    private void describeTableReturns(TableStatus first, TableStatus... rest) {
        OngoingStubbing<DescribeTableResult> stubbing = when(dynamoDb.describeTable(any(DescribeTableRequest.class)))
                .thenReturn(describeResult(first));
        for (TableStatus status : rest) {
            stubbing = stubbing.thenReturn(describeResult(status));
        }
    }

    private static DescribeTableResult describeResult(TableStatus status) {
        return new DescribeTableResult().withTable(new TableDescription().withTableName(TABLE).withTableStatus(status));
    }

    private static CreateTableRequest request() {
        return new CreateTableRequest().withTableName(TABLE);
    }
}
