package online.iwantagift.ui.services;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.PublicJwk;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.dto.auth.CredentialsDTO;
import online.iwantagift.ui.models.dto.auth.TokenDTO;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import online.iwantagift.ui.util.exceptions.HttpErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.PublicKey;

/**
 * Client for the authentication microservice.
 *
 * <p>This service caches the JWT verification key exposed by the auth service JWKS endpoint
 * and forwards sign-in, sign-up, and refresh requests to the corresponding auth endpoints.
 * The cached key is loaded lazily and replaced atomically when refreshed.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final IwagProperties iwagProperties;

    private String authServiceUrl;

    private final RestClient restClient = RestClient.create();

    private volatile PublicKey cachedPublicKey;

    @PostConstruct
    private void init() {
        var authService = iwagProperties.getRequiredService("auth");
        authServiceUrl = authService.getBaseUrl();
        log.info("Auth service client initialized for {}", authServiceUrl);
    }

    /**
     * Returns the public key used to verify JWT signatures.
     *
     * <p>If no key has been cached yet, this method fetches it from the auth service before
     * returning.
     *
     * @return the cached or freshly fetched JWT verification key
     * @throws IllegalStateException if the JWKS response is empty or does not contain the
     *     expected signing key
     */
    public PublicKey getPublicKey() {
        if (cachedPublicKey == null) {
            log.info("JWT public key cache is empty, refreshing");
            refreshPublicKey();
        }

        return cachedPublicKey;
    }

    /**
     * Replaces the cached JWT verification key with the current key from the auth service.
     *
     * <p>The cache update is synchronized so concurrent callers observe a fully replaced key.
     *
     * @throws IllegalStateException if the JWKS response is empty or does not contain the
     *     expected signing key
     */
    public void refreshPublicKey() {
        synchronized (this) {
            log.info("Refreshing JWT public key from auth service");
            cachedPublicKey = fetchPublicKey();
            log.info("JWT public key was refreshed");
        }
    }

    private PublicKey fetchPublicKey() {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path("/.well-known/jwks.json")
                .build()
                .toUri();
        log.debug("Requesting JWKS from {}", uri);

        String jwksJson = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (jwksJson == null || jwksJson.isBlank()) {
            throw new IllegalStateException("Auth service returned empty JWKS");
        }

        JwkSet set = Jwks.setParser().build().parse(jwksJson);

        for (Jwk<?> jwk : set.getKeys()) {
            if ("jwt-sign".equals(jwk.getId())
                    && jwk instanceof PublicJwk<?> pj
                    && pj.toKey() instanceof PublicKey pk) {
                log.info("Found jwt-sign public key in JWKS response");
                return pk;
            }
        }
        log.error("JWKS returned unknown jwks {}", set);

        throw new IllegalStateException("Public key with kid='jwt-sign' not found");
    }

    /**
     * Authenticates an existing user with the auth service.
     *
     * @param credentials the credentials to submit to the sign-in endpoint
     * @return the token payload returned by the auth service, or {@code null} if the response
     *     body is empty
     * @throws RemoteServiceException if the auth service reports an error response
     */
    public TokenDTO signIn(CredentialsDTO credentials) throws RemoteServiceException {
        return postForTokens("/auth/signin", credentials);
    }

    /**
     * Registers a new user with the auth service.
     *
     * @param credentials the credentials to submit to the sign-up endpoint
     * @return the token payload returned by the auth service, or {@code null} if the response
     *     body is empty
     * @throws RemoteServiceException if the auth service reports an error response
     */
    public TokenDTO signUp(CredentialsDTO credentials) throws RemoteServiceException {
        return postForTokens("/auth/signup", credentials);
    }

    private TokenDTO postForTokens(String path, Object body) throws RemoteServiceException {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path(path)
                .build()
                .toUri();
        log.debug("Sending auth request to {}", uri);
        var response = restClient.post()
                .uri(uri)
                .body(body)
                .retrieve();

        response.onStatus(new HttpErrorHandler());

        TokenDTO tokenDTO = response.body(TokenDTO.class);
        log.debug("Auth request completed for {}", path);
        return tokenDTO;
    }

    /**
     * Requests a new token payload from the auth service using a refresh token.
     *
     * @param refreshToken the refresh token to send to the refresh endpoint
     * @return the token payload returned by the auth service, or {@code null} if the response
     *     body is empty
     * @throws RemoteServiceException if the auth service reports an error response
     */
    public TokenDTO refresh(String refreshToken) throws RemoteServiceException {
        log.debug("Sending refresh request to auth service");
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path("/refresh")
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUri();

        var response = restClient.get()
                .uri(uri)
                .retrieve();

        response.onStatus(new HttpErrorHandler());

        TokenDTO tokenDTO = response.body(TokenDTO.class);
        log.debug("Refresh request completed");
        return tokenDTO;
    }
}
