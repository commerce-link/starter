package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DynamoDbLocalDateConverter implements DynamoDBTypeConverter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public String convert(LocalDate localDate) {
        return localDate.format(FORMATTER);
    }

    @Override
    public LocalDate unconvert(String string) {
        return LocalDate.parse(string, FORMATTER);
    }
}