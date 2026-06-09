package online.iwantagift.ui.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures the UI application's web security policy.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Builds the security filter chain for browser requests.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/font/**", "/fonts/**",
                                "/", "/landing", "/auth/*", "/error", "/error/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Replaces Spring's auto-generated "Login with OAuth 2.0" page:
                        // unauthenticated users are sent into the OAuth2 flow via /auth/signin.
                        .loginPage("/auth/signin")
                        .defaultSuccessUrl("/", true)
                        // OAuth2 failures (state mismatch, token/userinfo errors) land on a real page.
                        .failureUrl("/landing?error=login")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
