package pl.commercelink.starter.localization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
class MessageSourceEnumLocalizer implements EnumLocalizer {

    private final MessageSource messageSource;
    private final Locale fallbackLocale;

    MessageSourceEnumLocalizer(
            MessageSource messageSource,
            @Value("${commercelink.localization.fallback-locale:#{null}}") Locale fallbackLocale) {
        this.messageSource = messageSource;
        this.fallbackLocale = fallbackLocale;
    }

    @Override
    public String localize(Enum<?> value) {
        return localize(value, null, contextLocale());
    }

    @Override
    public String localize(Enum<?> value, Locale locale) {
        return localize(value, null, locale);
    }

    @Override
    public String localize(Enum<?> value, String suffix) {
        return localize(value, suffix, contextLocale());
    }

    @Override
    public String localize(Enum<?> value, String suffix, Locale locale) {
        if (value == null) return "";
        String key = value.getClass().getSimpleName() + "." + value.name();
        if (suffix != null && !suffix.isEmpty()) key += "." + suffix;
        return messageSource.getMessage(key, null, value.name(), resolveLocale(locale));
    }

    private Locale contextLocale() {
        return LocaleContextHolder.getLocaleContext() != null
                ? LocaleContextHolder.getLocale()
                : null;
    }

    private Locale resolveLocale(Locale locale) {
        if (locale != null) return locale;
        return fallbackLocale != null ? fallbackLocale : Locale.getDefault();
    }
}
