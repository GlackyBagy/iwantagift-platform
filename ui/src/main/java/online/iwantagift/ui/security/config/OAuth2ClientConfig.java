package online.iwantagift.ui.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Persists OAuth2 authorized clients (access/refresh tokens) in the database instead of the default
 * in-memory store. Keeping them in memory means the tokens are lost on every application restart,
 * which leaves the user's session valid (the OidcUser lives in the HTTP session) while
 * {@code requireAccessToken} can no longer find a token — surfacing as "OAuth2 access token is
 * missing". Backing the store with JDBC makes the tokens survive restarts.
 */
@Configuration
public class OAuth2ClientConfig {

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(
            JdbcOperations jdbcOperations,
            ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
    }

    /**
     * Service-backed manager used to obtain the current access token. It refreshes the token via the
     * stored refresh token when the access token has expired and re-persists the result through the
     * (JDBC-backed) {@link OAuth2AuthorizedClientService}. It is service-backed rather than
     * request-backed because the only context available at the call site is the {@code Authentication}
     * (no servlet request/response).
     */
    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }
}
