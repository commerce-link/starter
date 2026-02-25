package pl.commercelink.starter.autoconfigure;

import io.sentry.Sentry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

    @Bean
    @ConditionalOnProperty(name = "application.env", havingValue = "prod", matchIfMissing = false)
    public ServletContextListener sentryCleanupListener() {
        return new ServletContextListener() {

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                Sentry.close();
            }

            @Override
            public void contextInitialized(ServletContextEvent sce) {}
        };
    }
}
