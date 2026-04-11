package online.iwantagift.ui.security.jwt;

import jakarta.servlet.http.Cookie;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for JWT-related HTTP cookies used by the UI application.
 *
 * <p>The created cookies are marked {@code HttpOnly}, use the configured security flag, are scoped
 * to the application root path, and either carry token values or immediately expire to clear
 * authentication state in the browser.
 */
@Component
@Getter
public class JwtCookieFactory {

    @Value("${jwt.cookie.name.access}")
    private String accessCookieName;

    @Value("${jwt.cookie.name.refresh}")
    private String refreshCookieName;

    @Value("${jwt.cookie.max-age-seconds}")
    private int maxAgeSeconds;

    @Value("${jwt.cookie.set-secure}")
    private boolean secure;

    /**
     * Creates the cookies used to store the current authentication tokens.
     *
     * @param jwt the access-token value to store
     * @param refreshToken the refresh-token value to store
     * @return a list containing the access-token cookie and the second token cookie created for the
     *     supplied refresh-token value
     */
    public List<Cookie> createAuthCookies(String jwt, String refreshToken) {
        return List.of(createAccessCookie(jwt),
                createRefreshCookie(refreshToken));
    }

    private Cookie createAccessCookie(String jwt) {
        Cookie cookie = new Cookie(accessCookieName, jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    private Cookie createRefreshCookie(String refresh) {
        Cookie cookie = new Cookie(refreshCookieName, refresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    /**
     * Creates cookies that clear the stored authentication tokens in the browser.
     *
     * @return a list containing immediately expiring cookies for both configured token cookie names
     */
    public List<Cookie> createLogoutCookies() {
        return List.of(killAccessCookie(),
                killRefreshCookie());
    }

    private Cookie killAccessCookie() {
        Cookie cookie = new Cookie(accessCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    private Cookie killRefreshCookie() {
        Cookie cookie = new Cookie(refreshCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
