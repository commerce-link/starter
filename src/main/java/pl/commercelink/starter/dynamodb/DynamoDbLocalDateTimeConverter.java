package pl.commercelink.starter.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DynamoDbLocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convert(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }

    @Override
    public LocalDateTime unconvert(String string) {
        if(string.length()<12){
            return LocalDateTime.of(LocalDate.parse(string), LocalTime.NOON);
        }
        return LocalDateTime.parse(string, FORMATTER);
    }
}
