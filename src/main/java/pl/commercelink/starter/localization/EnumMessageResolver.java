package pl.commercelink.starter.localization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class EnumMessageResolver {
    private static MessageSource messageSource;

    @Autowired
    public EnumMessageResolver(@Qualifier("messageSource") MessageSource source) {
        EnumMessageResolver.messageSource = source;
    }

    public static String get(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null || locale == Locale.getDefault()) {
            locale = new Locale("pl");
        }
        return messageSource.getMessage(code, null, "", locale);
    }

    public static String get(String key, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}
