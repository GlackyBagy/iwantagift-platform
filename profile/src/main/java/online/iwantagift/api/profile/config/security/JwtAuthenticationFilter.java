package online.iwantagift.api.profile.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.profile.services.AuthService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final AuthService authService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Security context already contains authentication for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION));

        if (token.isEmpty()) {
            log.debug("No access token found for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = parseClaims(token.get());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principalFrom(claims),
                    null,
                    Collections.emptyList()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication was set from JWT for {}", request.getRequestURI());
        } catch (RuntimeException e) {
            log.debug("Invalid access token for {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String principalFrom(Claims claims) {
        String userId = claims.get("userId", String.class);
        return userId == null || userId.isBlank() ? claims.getSubject() : userId;
    }

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

    private String normalizeToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token is empty");
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
