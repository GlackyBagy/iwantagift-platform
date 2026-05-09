package online.iwantagift.ui.security.jwt;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtCookieFactoryTest {
    private static final String ACCESS_COOKIE_NAME = "access_token";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final int MAX_AGE_SECONDS = 3600;

    private JwtCookieFactory jwtCookieFactory;

    @BeforeEach
    void setUp() {
        jwtCookieFactory = new JwtCookieFactory();
        ReflectionTestUtils.setField(jwtCookieFactory, "accessCookieName", ACCESS_COOKIE_NAME);
        ReflectionTestUtils.setField(jwtCookieFactory, "refreshCookieName", REFRESH_COOKIE_NAME);
        ReflectionTestUtils.setField(jwtCookieFactory, "maxAgeSeconds", MAX_AGE_SECONDS);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createAuthCookies_returnsConfiguredTokenCookies(boolean secure) {
        ReflectionTestUtils.setField(jwtCookieFactory, "secure", secure);

        List<Cookie> cookies = jwtCookieFactory.createAuthCookies("jwt-value", "refresh-value");

        assertEquals(2, cookies.size());
        assertTokenCookie(cookies.get(0), ACCESS_COOKIE_NAME, "jwt-value", MAX_AGE_SECONDS, secure);
        assertTokenCookie(cookies.get(1), REFRESH_COOKIE_NAME, "refresh-value", MAX_AGE_SECONDS, secure);
    }

    @Test
    void createLogoutCookies_returnsExpiredEmptyTokenCookies() {
        ReflectionTestUtils.setField(jwtCookieFactory, "secure", true);

        List<Cookie> cookies = jwtCookieFactory.createLogoutCookies();

        assertEquals(2, cookies.size());
        assertTokenCookie(cookies.get(0), ACCESS_COOKIE_NAME, "", 0, true);
        assertTokenCookie(cookies.get(1), REFRESH_COOKIE_NAME, "", 0, true);
    }

    @Test
    void createLogoutCookies_usesConfiguredSecureFlag() {
        ReflectionTestUtils.setField(jwtCookieFactory, "secure", false);

        List<Cookie> cookies = jwtCookieFactory.createLogoutCookies();

        assertFalse(cookies.get(0).getSecure());
        assertFalse(cookies.get(1).getSecure());
    }

    private void assertTokenCookie(Cookie cookie, String name, String value, int maxAge, boolean secure) {
        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals(secure, cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(maxAge, cookie.getMaxAge());
    }
}
