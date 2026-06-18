package pl.commercelink.starter.localization;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
class MessageSourceEnumLocalizer implements EnumLocalizer {
    private final MessageSource messageSource;

    @Value("${commercelink.localization.fallback-locale:#{null}}")
    private Locale fallbackLocale;

    @Override
    public String localize(Enum<?> value) {
        return localize(value, null, LocaleContextHolder.getLocale());
    }

    @Override
    public String localize(Enum<?> value, Locale locale) {
        return localize(value, null, locale);
    }

    @Override
    public String localize(Enum<?> value, String suffix) {
        return localize(value, suffix, LocaleContextHolder.getLocale());
    }

    @Override
    public String localize(Enum<?> value, String suffix, Locale locale) {
        if (value == null) return "";
        String key = value.getClass().getSimpleName() + "." + value.name();
        if (suffix != null && !suffix.isEmpty()) key += "." + suffix;
        return messageSource.getMessage(key, null, value.name(), resolveLocale(locale));
    }

    private Locale resolveLocale(Locale locale) {
        if (fallbackLocale == null) return locale;
        if (locale == null || locale == Locale.getDefault()) return fallbackLocale;
        return locale;
    }
}
