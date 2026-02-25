package pl.commercelink.starter.localization;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalLocalizationHandler {

    @ModelAttribute
    public void addLangAttribute(HttpServletRequest request, Model model) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        String cleanQuery = "";
        if (query != null && !query.isEmpty()) {
            cleanQuery = Arrays.stream(query.split("&"))
                    .filter(p -> !p.startsWith("lang="))
                    .collect(Collectors.joining("&"));
            if (!cleanQuery.isEmpty()) {
                cleanQuery += "&";
            }
        }

        model.addAttribute("currentUri", uri);
        model.addAttribute("currentQuery", cleanQuery);
    }
}
