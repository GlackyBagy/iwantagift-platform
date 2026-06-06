package online.iwantagift.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.iwantagift.auth.advice.AuthAdvice;
import online.iwantagift.auth.messaging.kafka.AccountProducer;
import online.iwantagift.auth.models.entities.Account;
import online.iwantagift.auth.models.entities.AccountFactory;
import online.iwantagift.auth.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AccountService accountService;
    @Mock AccountFactory accountFactory;
    @Mock AccountProducer accountProducer;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(accountService, accountFactory, accountProducer);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AuthAdvice())
                .build();
    }

    @Test
    void signUp_validCredentials_returns201WithAccountData() throws Exception {
        UUID userId = UUID.randomUUID();
        Account account = Account.builder()
                .id(userId)
                .email("alice@example.com")
                .build();
        when(accountFactory.create(any())).thenReturn(account);
        doNothing().when(accountService).save(account);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "alice@example.com", "password1", "password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(accountProducer).sendOnCreate(account.getId(), "alice", account.getEmail());
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
        doThrow(new DataIntegrityViolationException("duplicate email")).when(accountService).save(any());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpJson("alice", "alice@example.com", "password1", "password1")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.email").value("Account with provided email already exists"));

        verifyNoInteractions(accountProducer);
    }

    private String signUpJson(String nickname, String email, String password, String confirm) throws Exception {
        HashMap<String, String> body = new HashMap<>();
        body.put("nickname", nickname);
        body.put("email", email);
        body.put("password", password);
        body.put("confirmPassword", confirm);
        return objectMapper.writeValueAsString(body);
    }
}
