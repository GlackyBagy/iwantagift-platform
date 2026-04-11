package online.iwantagift.ui.services;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.PublicJwk;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.IwagProperties;
import online.iwantagift.ui.models.dto.CredentialsDTO;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.util.AuthErrorHandler;
import online.iwantagift.ui.util.exceptions.AuthServiceException;
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

    private final IwagProperties iwagProperties;

    private String authServiceUrl;

    private String scheme;

    private final RestClient restClient = RestClient.create();

    private volatile PublicKey cachedPublicKey;

    @PostConstruct
    private void init() {
        var authService = iwagProperties.getRequiredService("auth");
        scheme = authService.isUseHttps() ? "https://" : "http://";
        authServiceUrl = scheme + authService.getUrl();
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
        if (cachedPublicKey == null)
            refreshPublicKey();

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
            cachedPublicKey = fetchPublicKey();
        }
    }

    private PublicKey fetchPublicKey() {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path("/.well-known/jwks.json")
                .build()
                .toUri();

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
                return pk;
            }
        }

        throw new IllegalStateException("Public key with kid='jwt-sign' not found");
    }

    /**
     * Authenticates an existing user with the auth service.
     *
     * @param credentials the credentials to submit to the sign-in endpoint
     * @return the token payload returned by the auth service, or {@code null} if the response
     *     body is empty
     * @throws AuthServiceException if the auth service reports an error response
     */
    public TokenDTO signIn(CredentialsDTO credentials) throws AuthServiceException {
        return postForTokens("/signin", credentials);
    }

    /**
     * Registers a new user with the auth service.
     *
     * @param credentials the credentials to submit to the sign-up endpoint
     * @return the token payload returned by the auth service, or {@code null} if the response
     *     body is empty
     * @throws AuthServiceException if the auth service reports an error response
     */
    public TokenDTO signUp(CredentialsDTO credentials) throws AuthServiceException {
        return postForTokens("/signup", credentials);
    }

    private TokenDTO postForTokens(String path, Object body) throws AuthServiceException {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path(path)
                .build()
                .toUri();
        System.out.println("Request POST " + uri);
        var response = restClient.post()
                .uri(uri)
                .body(body)
                .retrieve();

        AuthErrorHandler.handle(response);

        return response.body(TokenDTO.class);
    }

    /**
     * Requests a new token payload from the auth service using a refresh token.
     *
     * @param refreshToken the refresh token to send to the refresh endpoint
     * @return the token payload returned by the auth service, or {@code null} if the response
     *     body is empty
     * @throws AuthServiceException if the auth service reports an error response
     */
    public TokenDTO refresh(String refreshToken) throws AuthServiceException {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .scheme(scheme)
                .path("/refresh")
                .queryParam("token", refreshToken)
                .build()
                .toUri();

        var response = restClient.get()
                .uri(uri)
                .retrieve();

        AuthErrorHandler.handle(response);

        return response.body(TokenDTO.class);
    }
}
