package online.iwantagift.ui.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import online.iwantagift.ui.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    private static final String TOKEN = "jwt-token";

    @Mock
    private JwtService jwtService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest("GET", "/profile");
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_keepsExistingAuthentication() throws Exception {
        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken(
                "existing-user",
                null
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(existingAuthentication, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotAuthenticate_whenAccessTokenIsMissing() throws Exception {
        when(jwtService.extractAccessToken(request)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).isValid(TOKEN);
        verify(jwtService, never()).parseClaims(TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doesNotAuthenticate_whenAccessTokenIsInvalid() throws Exception {
        when(jwtService.extractAccessToken(request)).thenReturn(Optional.of(TOKEN));
        when(jwtService.isValid(TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).parseClaims(TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_setsAuthentication_whenAccessTokenIsValid() throws Exception {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn("user-id");
        when(jwtService.extractAccessToken(request)).thenReturn(Optional.of(TOKEN));
        when(jwtService.isValid(TOKEN)).thenReturn(true);
        when(jwtService.parseClaims(TOKEN)).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("user-id", authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertTrue(authentication.getAuthorities().isEmpty());
        verify(filterChain).doFilter(request, response);
    }
}
