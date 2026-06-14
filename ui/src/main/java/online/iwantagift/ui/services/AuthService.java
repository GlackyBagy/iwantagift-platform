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
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
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
    private final ClientHttpRequestFactory requestFactory;
    private RestClient restClient;

    private String authServiceUrl;

    @PostConstruct
    private void init() {
        var authService = iwagProperties.getRequiredService("auth");
        authServiceUrl = authService.getBaseUrl();
        restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
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

    public void sendEmailConfirmationMessage(String accessToken) {
        authorizedRequest("/auth/confirm/email", accessToken, null);
    }

    public void sendEmailChangeMessage(String accessToken, CredentialsDTO dto) {
        authorizedRequest("/auth/change/email", accessToken, dto);
    }

    public void sendPasswordChangeMessage(String accessToken, CredentialsDTO dto) {
        authorizedRequest("/auth/change/password", accessToken, dto);
    }

    private void authorizedRequest(String path, String accessToken, Object body) {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path(path)
                .build()
                .toUri();

        log.info("Sending authorized request to auth service: POST {} (body present: {})",
                uri, body != null);

        var request = restClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        if (body != null)
            request.body(body);

        // toBodilessEntity() is the terminal call that actually executes the request. Without it,
        // retrieve()/onStatus() only build the spec and nothing is ever sent to the auth service.
        request.retrieve()
                .onStatus(new HttpErrorHandler())
                .toBodilessEntity();

        log.info("Auth service accepted request: POST {}", uri);
    }
}
