package online.iwantagift.auth.controllers;

import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.JwtService;
import online.iwantagift.auth.services.RefreshTokenService;
import online.iwantagift.auth.util.exceptions.TokenNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies {@link RefreshController} endpoint contracts:
 * – /refresh with valid token returns 200 + new TokenDTO,
 * – empty token returns 400,
 * – invalid (revoked/expired) token returns 400,
 * – token not found returns 400.
 */
@ExtendWith(MockitoExtension.class)
class RefreshControllerTest {

    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    AccountService accountService;
    @Mock
    JwtService jwtService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RefreshController controller = new RefreshController(refreshTokenService, accountService, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void refresh_validToken_returns200WithNewTokenPair() throws Exception {
        UUID userId = UUID.randomUUID();
        Account account = Account.builder().id(userId).email("alice@example.com").build();

        when(refreshTokenService.isTokenValid("valid-token")).thenReturn(true);
        when(refreshTokenService.findUserIdByRefreshToken("valid-token")).thenReturn(userId);
        when(accountService.findById(userId)).thenReturn(Optional.of(account));
        when(refreshTokenService.rotate("valid-token")).thenReturn("new-refresh");
        when(jwtService.jwtFromUsername("alice@example.com")).thenReturn("new-jwt");

        mockMvc.perform(get("/refresh").param("refreshToken", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("new-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void refresh_emptyToken_returns400() throws Exception {
        mockMvc.perform(get("/refresh").param("refreshToken", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_invalidToken_returns400() throws Exception {
        when(refreshTokenService.isTokenValid("revoked-token")).thenReturn(false);

        mockMvc.perform(get("/refresh").param("refreshToken", "revoked-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_tokenNotFound_returns400() throws Exception {
        when(refreshTokenService.isTokenValid("unknown")).thenThrow(new TokenNotFoundException("not found"));

        mockMvc.perform(get("/refresh").param("refreshToken", "unknown"))
                .andExpect(status().isBadRequest());
    }
}
