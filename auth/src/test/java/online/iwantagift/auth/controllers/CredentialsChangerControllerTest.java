package online.iwantagift.auth.controllers;

import online.iwantagift.auth.messaging.kafka.CredentialsProducer;
import online.iwantagift.auth.models.dto.CredentialsDTO;
import online.iwantagift.auth.models.events.CredentialsUpdateEvent;
import online.iwantagift.auth.services.AccountService;
import online.iwantagift.auth.services.VerificationTokenService;
import online.iwantagift.auth.util.EmailVerificationUrlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialsChangerControllerTest {

    @Mock VerificationTokenService verificationTokenService;
    @Mock EmailVerificationUrlBuilder emailVerificationUrlBuilder;
    @Mock CredentialsProducer credentialsProducer;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AccountService accountService;
    @Mock Authentication authentication;
    @Mock BindingResult bindingResult;

    private CredentialsChangerController controller() {
        return new CredentialsChangerController(
                verificationTokenService,
                emailVerificationUrlBuilder,
                credentialsProducer,
                passwordEncoder,
                accountService
        );
    }

    @Test
    void changeEmail_generatesTokenAndSendsEmailChangeEvent() {
        CredentialsDTO dto = new CredentialsDTO();
        dto.setEmail("new@example.com");
        when(authentication.getName()).thenReturn("old@example.com");
        when(verificationTokenService.generateAndSaveForEmail("old@example.com", "new@example.com"))
                .thenReturn("token");
        when(emailVerificationUrlBuilder.buildForEmail("token"))
                .thenReturn("http://auth/auth/verify/email?token=token");

        controller().changeEmail(dto, authentication);

        ArgumentCaptor<CredentialsUpdateEvent> eventCaptor =
                ArgumentCaptor.forClass(CredentialsUpdateEvent.class);
        verify(credentialsProducer).sendEmailChange(eventCaptor.capture());
        assertEquals(new CredentialsUpdateEvent(
                "old@example.com",
                "new@example.com",
                "http://auth/auth/verify/email?token=token"
        ), eventCaptor.getValue());
    }

    @Test
    void changePassword_updatesPasswordDirectlyWithoutEmail() {
        CredentialsDTO dto = new CredentialsDTO();
        dto.setPassword("new-password");
        dto.setConfirmPassword("new-password");
        when(authentication.getName()).thenReturn("user@example.com");
        when(passwordEncoder.encode("new-password")).thenReturn("hash");
        when(accountService.updatePassword("user@example.com", "hash")).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller().changePassword(dto, bindingResult, authentication);

        verify(accountService).updatePassword("user@example.com", "hash");
        verifyNoInteractions(credentialsProducer);
    }
}
