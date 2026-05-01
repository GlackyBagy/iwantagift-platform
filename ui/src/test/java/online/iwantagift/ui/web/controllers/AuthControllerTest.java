package online.iwantagift.ui.web.controllers;

import jakarta.servlet.http.Cookie;
import online.iwantagift.ui.models.dto.CredentialsDTO;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.services.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtCookieFactory jwtCookieFactory;
    @MockitoBean
    private JwtService jwtService;

    private final String accessCookieName = "access";
    private final String refreshCookieName = "refresh";

    @Test
    void logout_createsLogoutCookie() throws Exception {
        when(jwtCookieFactory.createLogoutCookies()).thenReturn(
                List.of(
                        new Cookie(accessCookieName, ""),
                        new Cookie(refreshCookieName, "")
                ));

        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists(accessCookieName))
                .andExpect(cookie().exists(refreshCookieName))
                .andExpect(cookie().value(accessCookieName, ""))
                .andExpect(cookie().value(refreshCookieName, ""));

        verifyNoInteractions(authService);
    }

    @Test
    void signInPage_returnsSignInView() throws Exception {
        mockMvc.perform(get("/auth/signin")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signinPage"))
                .andExpect(model().attributeExists("credentialsDTO"));
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
    void signUp_validCredentials() throws Exception {
        Mockito.when(authService.signUp(Mockito.any(CredentialsDTO.class)))
                .thenReturn(new TokenDTO());
        Mockito.when(jwtCookieFactory.createAuthCookies(null, null)).thenReturn(
                List.of(
                        new Cookie(accessCookieName, "accessToken"),
                        new Cookie(refreshCookieName, "refreshToken")
                ));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "email@email.com")
                        .param("password", "password12345")
                        .param("confirmPassword", "password12345")
                        .param("nickname", "valid_nickname"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void signIn_validCredentials() throws Exception {
        Mockito.when(authService.signIn(Mockito.any(CredentialsDTO.class)))
                .thenReturn(new TokenDTO());
        Mockito.when(jwtCookieFactory.createAuthCookies(null, null)).thenReturn(
                List.of(
                        new Cookie(accessCookieName, "accessToken"),
                        new Cookie(refreshCookieName, "refreshToken")
                ));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "email@email.com")
                        .param("password", "password12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
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
        mockMvc.perform(validSignRequestWithoutEmail(false).param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "email"));

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "email",
            "aafa",
            "f@@af.fg453.;",
            " ",
            ""
    })
    void signIn_wrongEmail_returnsSignInPage(String email) throws Exception {
        mockMvc.perform(validSignRequestWithoutEmail(true).param("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signinPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "email"));

        verifyNoInteractions(authService);
    }

    private MockHttpServletRequestBuilder validSignRequestWithoutEmail(boolean signIn) {
        var res = post(signIn ? "/auth/signin" : "/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("password", "password12345")
                .param("nickname", "valid_nickname");
        return signIn ? res : res.param("confirmPassword", "password12345");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "foo",
            "844426"
    })
    void signUp_wrongPassword_returnsSignUpPage(String password) throws Exception {
        mockMvc.perform(validSignRequestWithoutPasswords(false)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "password"));

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "foo",
            "844426"
    })
    void signIn_wrongPassword_returnsSignInPage(String password) throws Exception {
        mockMvc.perform(validSignRequestWithoutPasswords(true)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signinPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "password"));

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @CsvSource({
            "password12345,different12345",
            "faergh98h543ghkoe,freg534123"
    })
    void signUp_passwordMismatch_returnsSignUpPage(String password, String confirmPassword) throws Exception {
        mockMvc.perform(validSignRequestWithoutPasswords(false)
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signupPage"))
                .andExpect(model().attributeHasFieldErrors("credentialsDTO", "confirmPassword"));

        verifyNoInteractions(authService);
    }

    private MockHttpServletRequestBuilder validSignRequestWithoutPasswords(boolean signIn) {
        var res = post(signIn ? "/auth/signin" : "/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "email@email.com");
        return signIn ? res : res.param("nickname", "valid_nickname");
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

    private MockHttpServletRequestBuilder validSignUpRequestWithoutNickname() {
        return post("/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("password", "password12345")
                .param("email", "email@email.com")
                .param("confirmPassword", "password12345");
    }
}
