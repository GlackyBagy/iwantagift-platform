package online.iwantagift.ui.services;

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.PublicJwk;
import jakarta.annotation.PostConstruct;
import online.iwantagift.ui.models.dto.CredentialsDTO;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.util.AuthErrorHandler;
import online.iwantagift.ui.util.exceptions.AuthServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.PublicKey;

@Service
public class AuthService {

    @Value("${iwag.services.auth.url}")
    private String authServiceUrl;

    private String scheme;

    private final RestClient restClient = RestClient.create();

    private volatile PublicKey cachedPublicKey;

    @PostConstruct
    private void init(@Value("${iwag.services.auth.use-https}") boolean useHttps) {
        scheme = useHttps ? "https" : "http";
    }

    public PublicKey getPublicKey() {
        if (cachedPublicKey == null)
            refreshPublicKey();

        return cachedPublicKey;
    }

    public void refreshPublicKey() {
        synchronized (this) {
            cachedPublicKey = fetchPublicKey();
        }
    }

    private PublicKey fetchPublicKey() {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .scheme(scheme)
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

    public TokenDTO signIn(CredentialsDTO credentials) throws AuthServiceException {
        return postForTokens("/signin", credentials);
    }

    public TokenDTO signUp(CredentialsDTO credentials) throws AuthServiceException {
        return postForTokens("/signup", credentials);
    }

    private TokenDTO postForTokens(String path, Object body) throws AuthServiceException {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
                .path(path)
                .build()
                .toUri();

        var response = restClient.post()
                .uri(uri)
                .body(body)
                .retrieve();

        AuthErrorHandler.handle(response);

        return response.body(TokenDTO.class);
    }

    public TokenDTO refresh(String refreshToken) throws AuthServiceException {
        URI uri = UriComponentsBuilder.fromUriString(authServiceUrl)
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