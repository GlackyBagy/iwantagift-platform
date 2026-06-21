package online.iwantagift.ui.web.interceptors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AuthPageRedirectInterceptorTest {
    private AuthPageRedirectInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        interceptor = new AuthPageRedirectInterceptor();
        request = new MockHttpServletRequest("GET", "/auth/signin");
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void preHandle_allowsRequest_whenAuthenticationIsMissing() throws Exception {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void preHandle_allowsRequest_whenAuthenticationIsAnonymous() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous-key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void preHandle_allowsRequest_whenAuthenticationIsNotAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user",
                null
        ));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void preHandle_redirectsToRoot_whenUserIsAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of()
        ));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/", response.getRedirectedUrl());
    }
}
