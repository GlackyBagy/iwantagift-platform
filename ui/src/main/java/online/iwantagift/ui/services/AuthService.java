package online.iwantagift.ui.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.dto.auth.AccountDTO;
import online.iwantagift.ui.models.dto.auth.CredentialsDTO;
import online.iwantagift.ui.util.exceptions.HttpErrorHandler;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Client for account operations exposed by the auth service.
 *
 * <p>Authentication and token issuance are handled by Spring Security OAuth2 login, not by this
 * service.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final IwagProperties iwagProperties;
    private final RestClient restClient = RestClient.create();

    private String authServiceUrl;

    @PostConstruct
    private void init() {
        var authService = iwagProperties.getRequiredService("auth");
        authServiceUrl = authService.getBaseUrl();
        log.info("Auth service client initialized for {}", authServiceUrl);
    }

    public AccountDTO signUp(CredentialsDTO credentials) throws RemoteServiceException {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path("/auth/signup")
                .build()
                .toUri();
        log.debug("Sending signup request to {}", uri);

        var response = restClient.post()
                .uri(uri)
                .body(credentials)
                .retrieve();

        response.onStatus(new HttpErrorHandler());
        return response.body(AccountDTO.class);
    }
}
