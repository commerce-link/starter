package pl.commercelink.starter.localization;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MessageSourceEnumLocalizerTest {

    enum SampleEnum { Foo, Bar }

    @Test
    void localizesEnumUsingClassNameAndValueAsKey() {
        MessageSource source = mock(MessageSource.class);
        Locale polish = Locale.forLanguageTag("pl");
        when(source.getMessage("SampleEnum.Foo", null, "Foo", polish)).thenReturn("Pierwszy");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

        assertEquals("Pierwszy", localizer.localize(SampleEnum.Foo, polish));
    }

    @Test
    void fallsBackToEnumNameWhenKeyMissing() {
        StaticMessageSource source = new StaticMessageSource();
        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

        assertEquals("Foo", localizer.localize(SampleEnum.Foo, Locale.forLanguageTag("pl")));
    }

    @Test
    void returnsEmptyStringForNullEnum() {
        MessageSource source = mock(MessageSource.class);
        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

        assertEquals("", localizer.localize(null));
        assertEquals("", localizer.localize(null, Locale.forLanguageTag("pl")));
        verifyNoInteractions(source);
    }

    @Test
    void usesLocaleContextHolderWhenLocaleNotProvided() {
        Locale english = Locale.forLanguageTag("en");
        LocaleContextHolder.setLocale(english);
        try {
            MessageSource source = mock(MessageSource.class);
            when(source.getMessage(eq("SampleEnum.Bar"), any(), eq("Bar"), eq(english)))
                .thenReturn("Second");

            MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

            assertEquals("Second", localizer.localize(SampleEnum.Bar));
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    @Test
    void localizesEnumWithSuffix() {
        MessageSource source = mock(MessageSource.class);
        Locale polish = Locale.forLanguageTag("pl");
        when(source.getMessage("SampleEnum.Foo.singular", null, "Foo", polish))
                .thenReturn("Pojedynczy");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

        assertEquals("Pojedynczy", localizer.localize(SampleEnum.Foo, "singular", polish));
    }

    @Test
    void nullSuffixBehavesAsNoSuffix() {
        MessageSource source = mock(MessageSource.class);
        Locale polish = Locale.forLanguageTag("pl");
        when(source.getMessage("SampleEnum.Foo", null, "Foo", polish)).thenReturn("Pierwszy");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

        assertEquals("Pierwszy", localizer.localize(SampleEnum.Foo, null, polish));
        assertEquals("Pierwszy", localizer.localize(SampleEnum.Foo, "", polish));
    }

    @Test
    void suffixOverloadUsesLocaleContextHolderWhenLocaleNotProvided() {
        Locale english = Locale.forLanguageTag("en");
        LocaleContextHolder.setLocale(english);
        try {
            MessageSource source = mock(MessageSource.class);
            when(source.getMessage(eq("SampleEnum.Bar.plural"), any(), eq("Bar"), eq(english)))
                    .thenReturn("Many");

            MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

            assertEquals("Many", localizer.localize(SampleEnum.Bar, "plural"));
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    @Test
    void fallbackAppliedWhenLocaleIsNullAndFallbackConfigured() {
        Locale polish = Locale.forLanguageTag("pl");
        MessageSource source = mock(MessageSource.class);
        when(source.getMessage("SampleEnum.Foo", null, "Foo", polish)).thenReturn("Pierwszy");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, polish);

        assertEquals("Pierwszy", localizer.localize(SampleEnum.Foo, (Locale) null));
    }

    @Test
    void fallbackAppliedWhenLocaleEqualsJvmDefaultAndFallbackConfigured() {
        Locale polish = Locale.forLanguageTag("pl");
        MessageSource source = mock(MessageSource.class);
        when(source.getMessage("SampleEnum.Foo", null, "Foo", polish)).thenReturn("Pierwszy");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, polish);

        assertEquals("Pierwszy", localizer.localize(SampleEnum.Foo, Locale.getDefault()));
    }

    @Test
    void explicitNonDefaultLocaleWinsOverFallback() {
        Locale polish = Locale.forLanguageTag("pl");
        Locale english = Locale.forLanguageTag("en");
        MessageSource source = mock(MessageSource.class);
        when(source.getMessage("SampleEnum.Foo", null, "Foo", english)).thenReturn("First");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, polish);

        assertEquals("First", localizer.localize(SampleEnum.Foo, english));
    }

    @Test
    void fallbackNotAppliedWhenNotConfigured() {
        MessageSource source = mock(MessageSource.class);
        Locale jvmDefault = Locale.getDefault();
        when(source.getMessage("SampleEnum.Foo", null, "Foo", jvmDefault)).thenReturn("FromDefault");

        MessageSourceEnumLocalizer localizer = new MessageSourceEnumLocalizer(source, null);

        assertEquals("FromDefault", localizer.localize(SampleEnum.Foo, jvmDefault));
    }
}
