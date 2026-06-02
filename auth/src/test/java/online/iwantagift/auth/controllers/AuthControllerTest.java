package online.iwantagift.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.iwantagift.auth.advice.AuthAdvice;
import online.iwantagift.auth.messaging.kafka.AccountProducer;
import online.iwantagift.auth.models.dto.CredentialsDTO;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.models.entities.AccountFactory;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.JwtService;
import online.iwantagift.auth.services.RefreshTokenService;
import online.iwantagift.auth.util.exceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies {@link AuthController} endpoint contracts:
 * – /auth/signup: returns 200 + TokenDTO on success, 400 on validation errors or
 *   password mismatch, 409 on duplicate email.
 * – /auth/signin: returns 200 + TokenDTO on success, 400 on validation errors or
 *   bad credentials.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock JwtService jwtService;
    @Mock AccountService accountService;
    @Mock AccountFactory accountFactory;
    @Mock RefreshTokenService refreshTokenService;
    @Mock AccountProducer accountProducer;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(jwtService, accountService, accountFactory, refreshTokenService, accountProducer);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AuthAdvice())
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    // POST /auth/signup
    // ──────────────────────────────────────────────────────────────

    @Test
    void signUp_validCredentials_returns200WithTokens() throws Exception {
        UUID userId = UUID.randomUUID();
        Account account = Account.builder().id(userId).email("alice@example.com").build();
        when(accountFactory.create(any())).thenReturn(account);
        doNothing().when(accountService).save(account);
        when(accountService.userIdByEmail("alice@example.com")).thenReturn(Optional.of(userId));
        when(jwtService.jwtFromCredentials(any(CredentialsDTO.class))).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshTokenByUserId(userId)).thenReturn("refresh-token");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "alice@example.com", "password1", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(accountProducer).sendOnCreate(account.getId(), any(), account.getEmail());
    }

    @Test
    void signUp_passwordMismatch_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "alice@example.com", "password1", "DIFFERENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.confirmPassword").exists());
    }

    @Test
    void signUp_blankEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "", "password1", "password1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void signUp_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "not-an-email", "password1", "password1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void signUp_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "alice@example.com", "short", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void signUp_blankNickname_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("", "alice@example.com", "password1", "password1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nickname").exists());
    }

    @Test
    void signUp_duplicateEmail_returns409() throws Exception {
        Account account = Account.builder().email("alice@example.com").build();
        when(accountFactory.create(any())).thenReturn(account);
        doThrow(DataIntegrityViolationException.class).when(accountService).save(any());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "alice@example.com", "password1", "password1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.email").value("Account with provided email already exists"));

        verifyNoInteractions(accountProducer);
    }

    // ──────────────────────────────────────────────────────────────
    // POST /auth/signin
    // ──────────────────────────────────────────────────────────────

    @Test
    void signIn_validCredentials_returns200WithTokens() throws Exception {
        UUID userId = UUID.randomUUID();
        when(jwtService.jwtFromCredentials(any(CredentialsDTO.class))).thenReturn("jwt-token");
        when(accountService.userIdByEmail("alice@example.com")).thenReturn(Optional.of(userId));
        when(refreshTokenService.createRefreshTokenByUserId(userId)).thenReturn("refresh-token");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInJson("alice@example.com", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void signIn_blankEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInJson("", "password1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void signIn_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInJson("alice@example.com", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void signIn_badCredentials_returns400() throws Exception {
        when(jwtService.jwtFromCredentials(any(CredentialsDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInJson("alice@example.com", "wrongpass")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.credentials").value("Bad credentials"));
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private String signUpJson(String nickname, String email, String password, String confirm) throws Exception {
        return objectMapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("nickname", nickname);
            put("email", email);
            put("password", password);
            put("confirmPassword", confirm);
        }});
    }

    private String signInJson(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("email", email);
            put("password", password);
        }});
    }
}
