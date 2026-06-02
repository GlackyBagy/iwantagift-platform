package online.iwantagift.api.profile.services;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.PublicJwk;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.profile.config.IwagProperties;
import online.iwantagift.api.profile.models.dto.auth.TokenDTO;
import online.iwantagift.api.profile.util.exceptions.HttpErrorHandler;
import online.iwantagift.api.profile.util.exceptions.RemoteServiceException;
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
}
