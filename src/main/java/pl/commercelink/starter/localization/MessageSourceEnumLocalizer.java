package pl.commercelink.starter.localization;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
class MessageSourceEnumLocalizer implements EnumLocalizer {
    private final MessageSource messageSource;

    MessageSourceEnumLocalizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String localize(Enum<?> value) {
        return localize(value, LocaleContextHolder.getLocale());
    }

    @Override
    public String localize(Enum<?> value, Locale locale) {
        if (value == null) return "";
        String key = value.getClass().getSimpleName() + "." + value.name();
        return messageSource.getMessage(key, null, value.name(), locale);
    }
}
