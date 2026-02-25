package pl.commercelink.starter.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import pl.commercelink.starter.security.filter.CustomTokenRefreshFilter;
import pl.commercelink.starter.security.handler.CustomAuthenticationSuccessHandler;
import pl.commercelink.starter.security.handler.CustomLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationSuccessHandler successHandler, CustomLogoutSuccessHandler logoutSuccessHandler, CustomTokenRefreshFilter tokenRefreshFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/",
                            "/env",
                            "/login"
                    ).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(tokenRefreshFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                        .loginPage("/login")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }

}
