package online.iwantagift.ui.web.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Redirects authenticated users away from authentication pages.
 *
 * <p>This interceptor is intended for routes such as sign-in and sign-up, where authenticated
 * users should be sent back to the application root instead of seeing auth forms again.
 */
@Component
public class AuthPageRedirectInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthPageRedirectInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean loggedIn = auth != null &&
                auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken);

        if (loggedIn) {
            log.debug("Authenticated user redirected from auth page: {}", request.getRequestURI());
            response.sendRedirect("/");
            return false;
        }

        log.debug("Unauthenticated access allowed for auth page: {}", request.getRequestURI());
        return true;
    }
}
