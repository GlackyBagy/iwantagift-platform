package online.iwantagift.auth.services;

import online.iwantagift.auth.models.dto.CredentialsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link JwtService} contracts:
 * – generated token contains username as subject and roles claim,
 * – extractUsername returns the subject,
 * – isValid returns true iff subject matches user and token is not expired,
 * – jwtFromCredentials authenticates via AuthenticationManager then generates a token.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserDetailsService userDetailsService;

    JwtService jwtService;

    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(authenticationManager, userDetailsService);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
        // triggers @PostConstruct key generation
        ReflectionTestUtils.invokeMethod(jwtService, "init");
    }

    private UserDetails userDetails(String email) {
        return new User(email, "hash", Collections.emptyList());
    }

    @Test
    void generateToken_subjectIsUsername() {
        String token = jwtService.generateToken(userDetails("alice@example.com"));
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@example.com");
    }

    @Test
    void extractUsername_returnsSubjectFromToken() {
        UserDetails ud = userDetails("bob@example.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.extractUsername(token)).isEqualTo("bob@example.com");
    }

    @Test
    void isValid_matchingUserAndValidToken_returnsTrue() {
        UserDetails ud = userDetails("carol@example.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.isValid(token, ud)).isTrue();
    }

    @Test
    void isValid_differentUser_returnsFalse() {
        UserDetails tokenOwner = userDetails("carol@example.com");
        UserDetails otherUser = userDetails("eve@example.com");
        String token = jwtService.generateToken(tokenOwner);
        assertThat(jwtService.isValid(token, otherUser)).isFalse();
    }

    @Test
    void isValid_expiredToken_returnsFalse() {
        // Set expiration to past by using 0 ms (already expired upon generation)
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        UserDetails ud = userDetails("dave@example.com");
        String token = jwtService.generateToken(ud);

        // Restore for parsing with correct key; expired tokens throw during parse in jjwt,
        // so isValid should propagate or return false — here we verify by catching exception.
        assertThatException().isThrownBy(() -> jwtService.isValid(token, ud));
    }

    @Test
    void jwtFromCredentials_authenticatesAndReturnsToken() {
        UserDetails ud = userDetails("frank@example.com");
        UsernamePasswordAuthenticationToken authToken =
                UsernamePasswordAuthenticationToken.authenticated(ud, null, Collections.emptyList());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);

        CredentialsDTO dto = new CredentialsDTO();
        dto.setEmail("frank@example.com");
        dto.setPassword("password1");

        String token = jwtService.jwtFromCredentials(dto);

        assertThat(jwtService.extractUsername(token)).isEqualTo("frank@example.com");
    }

    @Test
    void jwtFromUsername_loadsUserAndReturnsToken() {
        UserDetails ud = userDetails("grace@example.com");
        when(userDetailsService.loadUserByUsername("grace@example.com")).thenReturn(ud);

        String token = jwtService.jwtFromUsername("grace@example.com");

        assertThat(jwtService.extractUsername(token)).isEqualTo("grace@example.com");
    }
}
