package online.iwantagift.auth.jwt;

import jakarta.servlet.http.Cookie;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwtCookieFactory {

    @Value("${jwt.cookie-name}")
    private String cookieName;

    public Cookie createAuthCookie(String token, int maxAgeSeconds) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true в prod под HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

    public Cookie createLogoutCookie() {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // todo true в prod под HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}