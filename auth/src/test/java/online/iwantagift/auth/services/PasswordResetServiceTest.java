package online.iwantagift.auth.services;

import online.iwantagift.auth.messaging.kafka.CredentialsProducer;
import online.iwantagift.auth.models.events.PasswordResetEvent;
import online.iwantagift.auth.models.events.PasswordResetRequestEvent;
import online.iwantagift.auth.util.EmailVerificationUrlBuilder;
import online.iwantagift.auth.util.PasswordGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock PasswordResetTokenService passwordResetTokenService;
    @Mock AccountService accountService;
    @Mock PasswordGenerator passwordGenerator;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CredentialsProducer credentialsProducer;
    @Mock EmailVerificationUrlBuilder emailVerificationUrlBuilder;

    private PasswordResetService service() {
        return new PasswordResetService(
                passwordResetTokenService,
                accountService,
                passwordGenerator,
                passwordEncoder,
                credentialsProducer,
                emailVerificationUrlBuilder
        );
    }

    @Test
    void request_knownAccount_createsTokenAndSendsConfirmationLink() {
        when(accountService.userIdByEmail("user@example.com")).thenReturn(Optional.of(UUID.randomUUID()));
        when(passwordResetTokenService.create("user@example.com")).thenReturn(Optional.of("token"));
        when(emailVerificationUrlBuilder.buildForResetConfirm("token"))
                .thenReturn("http://auth/auth/reset/password/confirm?token=token");

        PasswordResetService.RequestOutcome outcome = service().request("user@example.com");

        assertEquals(PasswordResetService.RequestOutcome.SENT, outcome);
        verify(credentialsProducer).sendPasswordResetRequest(new PasswordResetRequestEvent(
                "user@example.com",
                "http://auth/auth/reset/password/confirm?token=token"
        ));
    }

    @Test
    void request_activeResetPending_returnsAlreadyPendingAndSendsNothing() {
        when(accountService.userIdByEmail("user@example.com")).thenReturn(Optional.of(UUID.randomUUID()));
        when(passwordResetTokenService.create("user@example.com")).thenReturn(Optional.empty());

        PasswordResetService.RequestOutcome outcome = service().request("user@example.com");

        assertEquals(PasswordResetService.RequestOutcome.ALREADY_PENDING, outcome);
        verifyNoInteractions(credentialsProducer);
    }

    @Test
    void request_unknownAccount_doesNotCreateTokenButReportsSent() {
        when(accountService.userIdByEmail("ghost@example.com")).thenReturn(Optional.empty());

        PasswordResetService.RequestOutcome outcome = service().request("ghost@example.com");

        assertEquals(PasswordResetService.RequestOutcome.SENT, outcome);
        verify(passwordResetTokenService, never()).create("ghost@example.com");
        verifyNoInteractions(credentialsProducer);
    }

    @Test
    void confirm_validToken_resetsPasswordAndEmailsIt() {
        when(passwordResetTokenService.consume("token")).thenReturn(Optional.of("user@example.com"));
        when(passwordGenerator.generate()).thenReturn("generated-pass");
        when(passwordEncoder.encode("generated-pass")).thenReturn("hash");
        when(accountService.updatePassword("user@example.com", "hash")).thenReturn(true);

        assertTrue(service().confirm("token"));

        verify(accountService).updatePassword("user@example.com", "hash");
        verify(credentialsProducer).sendPasswordReset(
                new PasswordResetEvent("user@example.com", "generated-pass"));
    }

    @Test
    void confirm_invalidToken_returnsFalseAndSendsNothing() {
        when(passwordResetTokenService.consume("missing")).thenReturn(Optional.empty());

        assertFalse(service().confirm("missing"));

        verifyNoInteractions(credentialsProducer);
        verifyNoInteractions(passwordGenerator);
    }
}
