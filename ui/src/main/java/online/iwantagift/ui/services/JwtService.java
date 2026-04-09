package online.iwantagift.ui.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

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

    public boolean isValid(String token) {
        try {
            parseClaims(normalizeToken(token));
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        String normalizedToken = normalizeToken(token);

        try {
            return parseClaimsWithKey(normalizedToken, authService.getPublicKey());
        } catch (JwtException e) {
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

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractCookieWithName(request, accessCookieName)
                .map(Cookie::getValue);
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractCookieWithName(request, refreshCookieName)
                .map(Cookie::getValue);
    }

    private Optional<Cookie> extractCookieWithName(HttpServletRequest request, String name) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }
}