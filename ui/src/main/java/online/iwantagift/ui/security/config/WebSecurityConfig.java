package online.iwantagift.ui.security.config;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures the UI application's web security policy.
 *
 * <p>Static assets, the landing page, and authentication pages are publicly accessible. All other
 * requests require an authenticated security context populated by the JWT cookie filter.
 * Form-login and default logout handling are disabled because authentication is managed through
 * custom token endpoints and cookies.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Builds the security filter chain for browser requests.
     *
     * <p>The chain permits unauthenticated access to public UI routes, requires authentication for
     * all remaining requests, and inserts {@link JwtAuthenticationFilter} before Spring Security's
     * username-password authentication filter.
     *
     * @param http the security builder used to configure the filter chain
     * @return the configured security filter chain
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/*", "/js/*", "/img/*", "/fonts/*",
                                "/", "/landing", "/auth/*")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
