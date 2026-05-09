package online.iwantagift.ui.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Optional;

/**
 * Service for validating JWTs issued by the authentication microservice.
 *
 * <p>This service verifies signed tokens with the public key provided by
 * {@link AuthService}. When signature validation fails, it refreshes the cached public key
 * once and retries claim parsing. It also extracts access and refresh tokens from configured
 * HTTP cookies.
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final AuthService authService;

    @Value("${jwt.cookie.name.access}")
    private String accessCookieName;

    @Value("${jwt.cookie.name.refresh}")
    private String refreshCookieName;

    private String normalizeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token is empty");
        }

        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    /**
     * Checks whether the supplied token can be parsed and verified with the current auth-service
     * signing key.
     *
     * <p>The token may be supplied either as a raw JWT or as a {@code Bearer } header value.
     * Validation failures are reported as {@code false}.
     *
     * @param token the token to validate
     * @return {@code true} if the token is structurally valid and signature verification succeeds;
     * {@code false} otherwise
     */
    public boolean isValid(String token) {
        try {
            parseClaims(normalizeToken(token));
            log.debug("JWT token validation succeeded");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses and verifies JWT claims using the auth-service public key.
     *
     * <p>If verification fails with the currently cached key, this method refreshes the key once
     * and retries parsing with the refreshed key.
     *
     * @param token the token to parse, either as a raw JWT or as a {@code Bearer } header value
     * @return the parsed JWT claims
     * @throws IllegalArgumentException if the token is {@code null}, blank, or otherwise invalid
     * @throws JwtException             if parsing or signature verification still fails after refreshing the key
     * @throws IllegalStateException    if the public key cannot be loaded from the auth service
     */
    public Claims parseClaims(String token) {
        String normalizedToken = normalizeToken(token);

        try {
            return parseClaimsWithKey(normalizedToken, authService.getPublicKey());
        } catch (JwtException e) {
            log.debug("JWT verification failed with cached key, refreshing public key and retrying");
            authService.refreshPublicKey();
            return parseClaimsWithKey(normalizedToken, authService.getPublicKey());
        }
    }

    private Claims parseClaimsWithKey(String token, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the configured access-token cookie value from the request.
     *
     * @param request the HTTP request to inspect
     * @return an {@link Optional} containing the cookie value when present, or an empty optional
     * otherwise
     * @throws NullPointerException if the request does not expose a cookie array
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        Optional<String> token = extractCookieWithName(request, accessCookieName)
                .map(Cookie::getValue);
        log.debug("Access token cookie {}",
                token.isPresent() ? "found" : "not found");
        return token;
    }

    /**
     * Extracts the configured refresh-token cookie value from the request.
     *
     * @param request the HTTP request to inspect
     * @return an {@link Optional} containing the cookie value when present, or an empty optional
     * otherwise
     * @throws NullPointerException if the request does not expose a cookie array
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        Optional<String> token = extractCookieWithName(request, refreshCookieName)
                .map(Cookie::getValue);
        log.debug("Refresh token cookie {}",
                token.isPresent() ? "found" : "not found");
        return token;
    }

    private Optional<Cookie> extractCookieWithName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        return cookies == null ? Optional.empty() : Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }
}
