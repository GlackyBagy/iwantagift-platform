package online.iwantagift.auth.config.security;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.services.AccountUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final AccountUserDetailsService userDetailsService;
    private final IwagProperties iwagProperties;

    /**
     * Bearer-protected REST endpoints that the ui calls server-to-server with an OAuth2 access token
     * (no session, no CSRF). These need a resource-server (JWT) chain — the form-login chain below
     * cannot authenticate a Bearer token, so without this the requests are bounced to /login and the
     * controller never runs.
     */
    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
        http
                .securityMatcher("/auth/change/**", "/auth/confirm/**", "/auth/deleteAccount", "/auth/deleteAll")
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @DependsOn("authenticationProvider")
    @Order(3)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws URISyntaxException {
        IwagProperties.ServiceProperties uiProps = iwagProperties.requireService("ui");
        String successRedirectUri = UriComponentsBuilder.fromUri(uiProps.requireBaseUrl().toURI())
                .path("/auth/signin")
                .toUriString();

        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/signup")
                        .permitAll()
                        // Password reset is a forgot-password flow: it must be reachable without a
                        // session. CSRF still applies (the reset pages render the token).
                        .requestMatchers("/auth/reset/password", "/auth/reset/password/**")
                        .permitAll()
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/signup"))
                .formLogin(form -> form
                        .loginPage("/login")
                        // Used only when there is no saved OAuth2 request (direct visit to /login):
                        // sends the user into the OAuth2 flow on the ui so the session is not "lost".
                        .defaultSuccessUrl(successRedirectUri)
                        .permitAll()
                )
                .logout(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @DependsOn("passwordEncoder")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
