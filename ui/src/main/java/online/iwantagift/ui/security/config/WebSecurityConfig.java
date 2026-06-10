package online.iwantagift.ui.security.config;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
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
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final IwagProperties iwagProperties;

    /**
     * Builds the security filter chain for browser requests.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // OAuth2 protocol failures (state mismatch, token/userinfo errors) are bounced back to the
        // auth service login page (login.html), which renders the error as a JS alert. The auth
        // base URL is browser-reachable (it is where the OAuth2 flow already redirects the user).
        String authErrorUrl = iwagProperties.getRequiredService("auth").getBaseUrl() + "/login?oauthError";

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
                        // OAuth2 failures are surfaced on the auth login page instead of /landing.
                        .failureUrl(authErrorUrl)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
