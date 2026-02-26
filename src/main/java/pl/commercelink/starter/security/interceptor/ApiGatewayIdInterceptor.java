package pl.commercelink.starter.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiGatewayIdInterceptor implements HandlerInterceptor {

    private static final String API_GATEWAY_ID_HEADER = "x-amzn-apigateway-api-id";

    @Value("${AWS_API_GATEWAY_ID:}")
    private String awsApiGatewayId;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String apiGatewayId = request.getHeader(API_GATEWAY_ID_HEADER);

        if (apiGatewayId == null || apiGatewayId.trim().isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing request source.\"}");
            return false;
        }

        if (StringUtils.isBlank(awsApiGatewayId) || !awsApiGatewayId.equalsIgnoreCase(apiGatewayId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid request source.\"}");
            return false;
        }

        return true;
    }
}