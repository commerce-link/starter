package pl.commercelink.starter.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * After signing in the user goes back to the page they opened (a link from an email, a QR code scanned from a printout),
 * or home when there is none. Only a page navigation is honoured: a background request the session expired on
 * (a list fragment, a status poll) would otherwise open as a bare fragment or JSON after login.
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler(@Value("${application.home}") String homeUrl) {
        setDefaultTargetUrl(homeUrl);
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null && !isPageNavigation(saved, request)) {
            requestCache.removeRequest(request, response);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /** Our scripts mark background requests with X-Requested-With ("fetch" or "XMLHttpRequest"); a page asks for HTML. */
    static boolean isPageNavigation(SavedRequest saved, HttpServletRequest current) {
        if (!"GET".equalsIgnoreCase(saved.getMethod()) || !saved.getHeaderValues("X-Requested-With").isEmpty()) {
            return false;
        }
        List<String> accept = saved.getHeaderValues("Accept");
        if (accept.stream().noneMatch(value -> value.contains("text/html"))) {
            return false;
        }
        String host = URI.create(saved.getRedirectUrl()).getHost();
        return host != null && host.equalsIgnoreCase(current.getServerName());
    }
}
