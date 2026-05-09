package online.iwantagift.ui.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @Mock
    private AuthService authService;
    @InjectMocks
    private JwtService jwtService;

    private static PublicKey publicKey;
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String ACCESS_TOKEN_NAME = "access_token";

    @BeforeAll
    static void readKeys() throws Exception {
        String publicPem = readPublicPemFromResource();
        String publicKeyPEM = publicPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] publicBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec publicSpec =
                new X509EncodedKeySpec(publicBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        publicKey = factory.generatePublic(publicSpec);
    }

    private static String readPublicPemFromResource() throws IOException {
        try (InputStream is = JwtServiceTest.class
                .getClassLoader()
                .getResourceAsStream("services/keys/public.pem")) {

            if (is == null)
                throw new FileNotFoundException("services/keys/public.pem");

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "refreshCookieName", REFRESH_COOKIE_NAME);
    }

    private void givenAuthServicePublicKey() {
        when(authService.getPublicKey()).thenReturn(publicKey);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/services/validJwtTokens.csv", numLinesToSkip = 1)
    void isValid_returnsTrueOnValid(String token) {
        givenAuthServicePublicKey();

        assertTrue(jwtService.isValid(token));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/services/invalidJwtTokens.csv")
    void isValid_returnsFalseOnInvalid(String token) {
        givenAuthServicePublicKey();

        assertFalse(jwtService.isValid(token));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/services/validJwtTokens.csv", numLinesToSkip = 1)
    void parseClaims_successOnValidToken(String token, String sub, String name, boolean admin, long iat) {
        givenAuthServicePublicKey();

        Claims claims = jwtService.parseClaims(token);

        assertEquals(sub, claims.getSubject());
        assertEquals(name, claims.get("name"));
        assertEquals(admin, claims.get("admin"));
        assertEquals(iat, claims.getIssuedAt().toInstant().getEpochSecond());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/services/invalidJwtTokens.csv")
    void parseClaims_jwtExceptionOnInvalidToken(String token) {
        givenAuthServicePublicKey();

        assertThrows(JwtException.class, () -> jwtService.parseClaims(token));
    }

    @Test
    void extractRefreshToken_returnsRefreshCookieValue_whenCookieExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie(ACCESS_TOKEN_NAME, "access-token-value"),
                new Cookie(REFRESH_COOKIE_NAME, "refresh-token-value")
        });

        assertEquals(Optional.of("refresh-token-value"), jwtService.extractRefreshToken(request));
    }

    @Test
    void extractRefreshToken_returnsEmpty_whenRefreshCookieIsMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("unknown_cookie", "unknown-cookie-value")
        });

        assertTrue(jwtService.extractRefreshToken(request).isEmpty());
    }

    @Test
    void extractRefreshToken_returnsEmpty_whenRefreshCookieIsEmpty() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertTrue(jwtService.extractRefreshToken(request).isEmpty());
    }

    @Test
    void extractRefreshToken_returnsEmpty_whenRequestHasEmptyCookieArray() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[0]);

        assertTrue(jwtService.extractRefreshToken(request).isEmpty());
    }

}
