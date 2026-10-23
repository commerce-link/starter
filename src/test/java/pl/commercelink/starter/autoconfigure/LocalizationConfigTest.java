package pl.commercelink.starter.autoconfigure;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationConfigTest {

    private static final String JUNK = "pizza:pizza=pizza";

    private LocalizationConfig config;
    private LocaleResolver localeResolver;
    private LocaleChangeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        config = new LocalizationConfig();
        ReflectionTestUtils.setField(config, "applicationLocale", "pl");
        localeResolver = config.localeResolver();
        interceptor = config.localeChangeInterceptor();
    }

    @Test
    void switchesLocaleWithLangParameter() throws Exception {
        MockHttpServletRequest request = requestWithResolver();
        request.setParameter("lang", "en");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));

        assertEquals(Locale.ENGLISH, localeResolver.resolveLocale(request));
    }

    @Test
    void ignoresJunkLangParameterAndKeepsDefaultLocale() throws Exception {
        MockHttpServletRequest request = requestWithResolver();
        request.setParameter("lang", JUNK);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));

        assertEquals(Locale.forLanguageTag("pl"), localeResolver.resolveLocale(request));
    }

    @Test
    void ignoresJunkLocaleCookieAndFallsBackToDefaultLocale() {
        MockHttpServletRequest request = requestWithResolver();
        request.setCookies(new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, JUNK));

        assertEquals(Locale.forLanguageTag("pl"), localeResolver.resolveLocale(request));
    }

    private MockHttpServletRequest requestWithResolver() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dashboard");
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);
        return request;
    }
}
