package online.iwantagift.ui.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.services.JwtService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Security filter that authenticates requests from the access-token cookie.
 *
 * <p>If the security context is already populated, the filter leaves it unchanged. Otherwise, it
 * reads the configured access token from the request cookies, validates it with
 * {@link JwtService}, and stores a {@link UsernamePasswordAuthenticationToken} containing the JWT
 * subject as the principal.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

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

        Optional<String> token = jwtService.extractAccessToken(request);

        if (token.isEmpty()) {
            log.debug("No access token cookie found for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.isValid(token.get())) {
            Claims claims = jwtService.parseClaims(token.get());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    Collections.emptyList()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication was set from JWT for {}", request.getRequestURI());
        } else {
            log.debug("Invalid access token for {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
