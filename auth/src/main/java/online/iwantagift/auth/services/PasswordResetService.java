package online.iwantagift.auth.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.messaging.kafka.MailRequestProducer;
import online.iwantagift.auth.models.events.PasswordResetEvent;
import online.iwantagift.auth.models.events.MailRequestEvent;
import online.iwantagift.auth.util.EmailVerificationUrlBuilder;
import online.iwantagift.auth.util.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Coordinates the (unauthenticated) password-reset flow:
 * <ol>
 *     <li>{@link #request(String)} reserves a token and emails a confirmation link;</li>
 *     <li>{@link #confirm(String)} generates a new password, stores it and emails it;</li>
 *     <li>{@link #cancel(String)} discards a pending reset.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenService passwordResetTokenService;
    private final AccountService accountService;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final MailRequestProducer mailRequestProducer;
    private final EmailVerificationUrlBuilder emailVerificationUrlBuilder;

    public enum RequestOutcome {
        /** A confirmation link was sent (or the account does not exist — indistinguishable to the caller). */
        SENT,
        /** A reset is already pending for this account; the token has not yet expired. */
        ALREADY_PENDING
    }

    public RequestOutcome request(String email) {
        // Unknown accounts get the same generic outcome but never trigger a token or email, so the
        // confirmation endpoint cannot be used to spam arbitrary inboxes.
        if (accountService.userIdByEmail(email).isEmpty()) {
            return RequestOutcome.SENT;
        }

        Optional<String> token = passwordResetTokenService.create(email);
        if (token.isEmpty()) {
            return RequestOutcome.ALREADY_PENDING;
        }

        String confirmUrl = emailVerificationUrlBuilder.buildForResetConfirm(token.get());
        mailRequestProducer.sendPasswordResetRequest(new MailRequestEvent(email, confirmUrl));
        return RequestOutcome.SENT;
    }

    /**
     * Confirms a reset: generates a new password, stores it (hashed) and emails the plaintext.
     *
     * @return {@code true} if the token was valid and the password was reset.
     */
    public boolean confirm(String token) {
        Optional<String> email = passwordResetTokenService.consume(token);
        if (email.isEmpty()) {
            return false;
        }

        String newPassword = passwordGenerator.generate();
        String passwordHash = passwordEncoder.encode(newPassword);
        if (!accountService.updatePassword(email.get(), passwordHash)) {
            return false;
        }

        mailRequestProducer.sendPasswordReset(new PasswordResetEvent(email.get(), newPassword));
        return true;
    }

    public void cancel(String token) {
        passwordResetTokenService.cancel(token);
    }
}
