package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.util.List;
import java.util.stream.Collectors;

public class DynamoDbEnumListConverter<T extends Enum<T>> implements DynamoDBTypeConverter<List<String>, List<T>> {

    private final Class<T> enumType;

    public DynamoDbEnumListConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public List<String> convert(List<T> enums) {
        return enums.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public List<T> unconvert(List<String> enumStrings) {
        return enumStrings.stream()
                .map(value -> Enum.valueOf(enumType, value))
                .collect(Collectors.toList());
    }
}
