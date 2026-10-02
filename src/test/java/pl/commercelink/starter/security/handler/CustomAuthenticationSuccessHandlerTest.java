package pl.commercelink.starter.security.handler;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static org.assertj.core.api.Assertions.assertThat;

/** Where a user lands after signing in: the page they opened (a scanned order link), or home for anything else. */
class CustomAuthenticationSuccessHandlerTest {

    private static final String HOME = "/dashboard";
    private static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

    private final CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler(HOME);
    private final MockHttpSession session = new MockHttpSession();

    @Test
    void redirectsToSavedPageAfterLogin() throws Exception {
        // given
        save(page("/dashboard/scan/orders/s1/o1"));

        // when
        MockHttpServletResponse response = login();

        // then: Spring Security 6 appends "continue" to the saved address; the page ignores it
        assertThat(response.getRedirectedUrl()).startsWith("http://localhost/dashboard/scan/orders/s1/o1");
    }

    @Test
    void redirectsHomeWithoutSavedRequest() throws Exception {
        // when
        MockHttpServletResponse response = login();

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(HOME);
    }

    @Test
    void redirectsHomeForBackgroundFetch() throws Exception {
        // given: list-page.js and async-form.js send "fetch", which Spring's default cache matcher does not exclude
        MockHttpServletRequest fragment = page("/dashboard/orders/list?page=2");
        fragment.addHeader("X-Requested-With", "fetch");
        save(fragment);

        // when
        MockHttpServletResponse response = login();

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(HOME);
        assertThat(session.getAttribute(SAVED_REQUEST)).isNull();
    }

    @Test
    void redirectsHomeForJsonRequest() throws Exception {
        // given
        MockHttpServletRequest json = get("/dashboard/orders/o1/shipments/status");
        json.addHeader("Accept", "application/json");
        save(json);

        // when
        MockHttpServletResponse response = login();

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(HOME);
    }

    @Test
    void redirectsHomeWhenThePageWasNotAskedForAsHtml() throws Exception {
        // given: a bare fetch() sends Accept: */*
        MockHttpServletRequest any = get("/dashboard/orders/o1/fragment");
        any.addHeader("Accept", "*/*");
        save(any);

        // when
        MockHttpServletResponse response = login();

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(HOME);
    }

    @Test
    void redirectsHomeForForeignHost() throws Exception {
        // given
        MockHttpServletRequest foreign = page("/dashboard/orders/o1");
        foreign.setServerName("evil.example");
        save(foreign);

        // when
        MockHttpServletResponse response = login();

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(HOME);
    }

    private MockHttpServletRequest get(String uriWithQuery) {
        String[] parts = uriWithQuery.split("\\?", 2);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", parts[0]);
        if (parts.length > 1) {
            request.setQueryString(parts[1]);
        }
        request.setSession(session);
        return request;
    }

    private MockHttpServletRequest page(String uriWithQuery) {
        MockHttpServletRequest request = get(uriWithQuery);
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return request;
    }

    private void save(MockHttpServletRequest request) {
        new HttpSessionRequestCache().saveRequest(request, new MockHttpServletResponse());
    }

    private MockHttpServletResponse login() throws Exception {
        MockHttpServletRequest callback = new MockHttpServletRequest("GET", "/login/oauth2/code/cognito");
        callback.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(callback, response, new TestingAuthenticationToken("user", "n/a"));
        return response;
    }
}
