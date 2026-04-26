package online.iwantagift.ui.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.services.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Handles browser token refresh requests for the UI application.
 *
 * <p>This controller reads the refresh token from cookies, requests a new token pair from the auth
 * service, updates browser cookies on success, and clears authentication cookies on failure.
 */
@Controller
@RequiredArgsConstructor
public class AuthRefreshController {
    private static final Logger log = LoggerFactory.getLogger(AuthRefreshController.class);

    private final JwtCookieFactory jwtCookieFactory;
    private final AuthService authClient;
    private final JwtService jwtService;

    /**
     * Refreshes authentication cookies using the refresh token from the current request.
     *
     * <p>If the refresh token is missing, blank, or rejected by the auth service, this method
     * clears authentication cookies and redirects the user to the sign-in page. On success, it
     * stores the refreshed tokens in cookies and redirects to a sanitized local target.
     *
     * @param redirect the optional redirect target requested by the client
     * @param request the current HTTP request
     * @param response the HTTP response that receives updated or clearing cookies
     * @return a redirect to the sanitized target on success, or to the sign-in page on failure
     */
    @GetMapping("/auth/refresh")
    public String refresh(@RequestParam(name = "redirect", required = false) String redirect,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        log.info("Handling refresh request with redirect='{}'", redirect);
        Optional<String> refreshToken = jwtService.extractRefreshToken(request);

        if (refreshToken.isEmpty() || refreshToken.get().isBlank()) {
            log.info("Refresh token is missing or blank, redirecting to sign-in");
            clearAuthCookies(response);
            return "redirect:/auth/signin";
        }

        try {
            TokenDTO tokens = authClient.refresh(refreshToken.get());

            jwtCookieFactory.createAuthCookies(tokens.getJwtToken(), tokens.getRefreshToken())
                    .forEach(response::addCookie);

            String safeRedirect = sanitizeRedirect(redirect);
            log.info("Token refresh succeeded, redirecting to '{}'", safeRedirect);
            return "redirect:" + safeRedirect;
        } catch (Exception ex) {
            log.warn("Token refresh failed: {}", ex.getMessage());
            clearAuthCookies(response);
            return "redirect:/auth/signin";
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        log.debug("Clearing authentication cookies");
        jwtCookieFactory.createLogoutCookies()
                .forEach(response::addCookie);
    }

    /**
     * Normalizes a redirect target so that only local application paths are allowed.
     *
     * <p>Absolute URIs, host-qualified values, malformed values, and blank paths are replaced with
     * the application root path.
     *
     * @param redirect the requested redirect target
     * @return a safe local redirect target beginning with {@code /}
     */
    private String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            log.info("Empty redirect target, using '/'");
            return "/";
        }

        try {
            URI uri = new URI(redirect);

            if (uri.isAbsolute() || uri.getHost() != null) {
                log.debug("Rejected non-local redirect target '{}', using '/'", redirect);
                return "/";
            }

            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            String fragment = uri.getRawFragment();

            if (path == null || path.isBlank() || !path.startsWith("/")) {
                log.debug("Rejected malformed redirect path '{}', using '/'", redirect);
                return "/";
            }

            StringBuilder safe = new StringBuilder(path);

            if (query != null && !query.isBlank())
                safe.append('?').append(query);

            if (fragment != null && !fragment.isBlank())
                safe.append('#').append(fragment);

            log.info("Sanitized redirect '{}' to '{}'", redirect, safe);
            return safe.toString();
        } catch (URISyntaxException e) {
            log.debug("Rejected invalid redirect URI '{}', using '/'", redirect);
            return "/";
        }
    }
}
