package online.iwantagift.ui.web.controllers;

import online.iwantagift.ui.models.dto.auth.AccountDTO;
import online.iwantagift.ui.models.dto.auth.CredentialsDTO;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.web.controllers.auth.AuthController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void signIn_redirectsToOauthLogin() throws Exception {
        mockMvc.perform(get("/auth/signin")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/iwag-ui"));
    }

    @Test
    void signUpPage_returnsSignUpView() throws Exception {
        mockMvc.perform(get("/auth/signup")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeExists("credentialsDTO"));
    }

    @Test
    void signUp_validCredentials_redirectsToOauthLogin() throws Exception {
        when(authService.signUp(any(CredentialsDTO.class)))
                .thenReturn(new AccountDTO(UUID.randomUUID(), "valid_nickname", "email@email.com"));

        mockMvc.perform(validSignUpRequest())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/iwag-ui"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "email",
            "aafa",
            "f@@af.fg453.;",
            " ",
            ""
    })
    void signUp_wrongEmail_returnsSignUpPage(String email) throws Exception {
        mockMvc.perform(validSignUpRequest().param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "email"));

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "foo",
            "844426"
    })
    void signUp_wrongPassword_returnsSignUpPage(String password) throws Exception {
        mockMvc.perform(validSignUpRequestWithoutPasswords()
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "password"));

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @CsvSource({
            "password12345,different12345",
            "faergh98h543ghkoe,freg534123"
    })
    void signUp_passwordMismatch_returnsSignUpPage(String password, String confirmPassword) throws Exception {
        mockMvc.perform(validSignUpRequestWithoutPasswords()
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "confirmPassword"));

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "QEfhnguy2egrhgkgrhgkqetgriuqetgriugkqetgriuykffg324hgrkbwiekujgyhowieuljkgh"})
    void signUp_wrongNickname_returnsSignUpPage(String nick) throws Exception {
        mockMvc.perform(validSignUpRequestWithoutNickname()
                        .param("nickname", nick))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "nickname"));

        verifyNoInteractions(authService);
    }

    private MockHttpServletRequestBuilder validSignUpRequest() {
        return validSignUpRequestWithoutNickname()
                .param("nickname", "valid_nickname");
    }

    private MockHttpServletRequestBuilder validSignUpRequestWithoutNickname() {
        return validSignUpRequestWithoutPasswords()
                .param("password", "password12345")
                .param("confirmPassword", "password12345");
    }

    private MockHttpServletRequestBuilder validSignUpRequestWithoutPasswords() {
        return post("/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "email@email.com");
    }
}
