package online.iwantagift.ui.security.jwt;

import jakarta.servlet.http.Cookie;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class JwtCookieFactory {

    @Value("${jwt.cookie.name.access}")
    private String accessCookieName;

    @Value("${jwt.cookie.name.refresh}")
    private String refreshCookieName;

    @Value("${jwt.cookie.max-age-seconds")
    private int maxAgeSeconds;

    @Value("${jwt.cookie.set-secure")
    private boolean secure;

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

    private Cookie createRefreshCookie(String refreshCookieName) {
        Cookie cookie = new Cookie(accessCookieName, refreshCookieName);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

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