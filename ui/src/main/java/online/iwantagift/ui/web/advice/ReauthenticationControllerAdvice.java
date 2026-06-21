package online.iwantagift.ui.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.util.exceptions.ReauthenticationRequiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Recovers from an unusable OAuth2 session by restarting the login flow.
 *
 * <p>When {@link ReauthenticationRequiredException} is raised (the access token could not be
 * obtained or refreshed), the local session is invalidated and the user is redirected to
 * {@code /settings}. Because the session is gone, that request is no longer authenticated, so the
 * security chain sends the user through the OAuth2 login flow, which mints fresh tokens. This turns
 * what was a 500 into a transparent re-login.
 */
@ControllerAdvice
@Slf4j
public class ReauthenticationControllerAdvice {

    @ExceptionHandler(ReauthenticationRequiredException.class)
    public String handleReauthentication(ReauthenticationRequiredException ex,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        log.warn("Re-authentication required, invalidating session: {}", ex.getMessage());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return "redirect:/settings";
    }
}
