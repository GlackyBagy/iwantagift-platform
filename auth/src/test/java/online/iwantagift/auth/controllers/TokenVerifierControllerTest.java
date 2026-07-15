package online.iwantagift.auth.controllers;

import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.models.dto.VerificationViaEmail;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenVerifierControllerTest {

    @Mock VerificationTokenService verificationTokenService;
    @Mock AccountService accountService;

    private final IwagProperties iwagProperties = new IwagProperties();

    @BeforeEach
    void setUp() throws MalformedURLException {
        configureUiBaseUrl("https://ui.example.com");
    }

    @Test
    void verifyEmail_changedEmail_updatesEmailRevokesTokenAndRedirects() throws MalformedURLException {
        configureUiBaseUrl("https://ui.example.com");
        when(verificationTokenService.findEmailChange("token")).thenReturn(Optional.of(
                new VerificationViaEmail("old@example.com", "new@example.com")
        ));
        when(accountService.updateEmail("old@example.com", "new@example.com")).thenReturn(true);
        TokenVerifierController controller = controller();

        assertEquals(
                "redirect:https://ui.example.com/settings",
                controller.verifyEmail("token")
        );
        verify(accountService).updateEmail("old@example.com", "new@example.com");
        verify(verificationTokenService).revoke("token");
    }

    @Test
    void verifyEmail_unchangedEmail_verifiesEmailAndRedirects() throws MalformedURLException {
        configureUiBaseUrl("https://ui.example.com/");
        when(verificationTokenService.findEmailChange("token")).thenReturn(Optional.of(
                new VerificationViaEmail("user@example.com", "user@example.com")
        ));
        when(accountService.verifyEmail("user@example.com")).thenReturn(true);
        TokenVerifierController controller = controller();

        assertEquals(
                "redirect:https://ui.example.com/settings",
                controller.verifyEmail("token")
        );
        verify(accountService).verifyEmail("user@example.com");
        verify(verificationTokenService).revoke("token");
    }

    @Test
    void verifyEmail_missingToken_throwsNotFound() {
        when(verificationTokenService.findEmailChange("missing")).thenReturn(Optional.empty());
        TokenVerifierController controller = controller();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.verifyEmail("missing")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(verificationTokenService, never()).revoke("missing");
    }

    @Test
    void verifyEmail_accountUpdateFailed_throwsBadRequestAndKeepsToken() {
        when(verificationTokenService.findEmailChange("token")).thenReturn(Optional.of(
                new VerificationViaEmail("old@example.com", "new@example.com")
        ));
        when(accountService.updateEmail("old@example.com", "new@example.com")).thenReturn(false);
        TokenVerifierController controller = controller();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.verifyEmail("token")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(verificationTokenService, never()).revoke("token");
    }

    private TokenVerifierController controller() {
        TokenVerifierController controller = new TokenVerifierController(
                verificationTokenService,
                accountService,
                iwagProperties
        );
        controller.init();
        return controller;
    }

    private void configureUiBaseUrl(String baseUrl) throws MalformedURLException {
        IwagProperties.ServiceProperties uiProps = new IwagProperties.ServiceProperties();
        uiProps.setBaseUrl(URI.create(baseUrl).toURL());
        iwagProperties.getServices().put("ui", uiProps);
    }
}
