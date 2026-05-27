package pl.commercelink.starter.localization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class EnumLocalizationAdviceTest {

    @Test
    void exposesInjectedEnumLocalizerAsModelAttribute() {
        EnumLocalizer expected = mock(EnumLocalizer.class);
        EnumLocalizationAdvice advice = new EnumLocalizationAdvice(expected);

        assertSame(expected, advice.enumI18n());
    }
}
