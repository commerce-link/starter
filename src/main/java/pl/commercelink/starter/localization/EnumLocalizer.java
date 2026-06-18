package pl.commercelink.starter.localization;

import java.util.Locale;

public interface EnumLocalizer {
    String localize(Enum<?> value);
    String localize(Enum<?> value, Locale locale);
    String localize(Enum<?> value, String suffix);
    String localize(Enum<?> value, String suffix, Locale locale);
}
