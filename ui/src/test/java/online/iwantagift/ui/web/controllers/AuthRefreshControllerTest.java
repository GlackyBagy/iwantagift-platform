package online.iwantagift.ui.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import online.iwantagift.ui.models.dto.TokenDTO;
import online.iwantagift.ui.security.jwt.JwtCookieFactory;
import online.iwantagift.ui.services.AuthService;
import online.iwantagift.ui.services.JwtService;
import online.iwantagift.ui.util.UriSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthRefreshController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthRefreshControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtCookieFactory jwtCookieFactory;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;

    @Test
    void refresh_redirectsToLoginPage_whenRefreshTokenIsInvalid() throws Exception {
        when(jwtService.extractRefreshToken(any(HttpServletRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/refresh")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/signin"));

        verify(jwtCookieFactory, times(1)).createLogoutCookies();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/me", "/profile?id=1344535#description",
            "/profile?id=1344535", "/donate/wish?id=13445&uid=4458vhg9564", ""})
    void refresh_redirectsToRootPage_whenRefreshTokenIsValid(String redirectTo) throws Exception {
        String sanitizedRedirectTo = UriSanitizer.sanitizeRedirect(redirectTo, Mockito.mock(Logger.class));

        String refreshToken = "refreshToken";
        TokenDTO refreshed = new TokenDTO();
        refreshed.setRefreshToken("updatedRefresh");
        refreshed.setJwtToken("updatedJwt");

        when(jwtService.extractRefreshToken(any(HttpServletRequest.class)))
                .thenReturn(Optional.of(refreshToken));

        when(authService.refresh(refreshToken)).thenReturn(refreshed);
        when(jwtCookieFactory.createAuthCookies(refreshed.getJwtToken(), refreshed.getRefreshToken()))
                .thenReturn(List.of());

        mockMvc.perform(get("/auth/refresh")
                        .accept(MediaType.TEXT_HTML)
                        .param("redirect", redirectTo))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(sanitizedRedirectTo));

        verify(authService, times(1)).refresh(refreshToken);
        verify(jwtCookieFactory, times(1)).createAuthCookies(refreshed.getJwtToken(), refreshed.getRefreshToken());
    }
}
