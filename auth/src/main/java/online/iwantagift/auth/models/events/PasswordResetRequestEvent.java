package online.iwantagift.auth.models.events;

/**
 * Emitted when a password reset is requested but not yet confirmed.
 *
 * <p>The mail service delivers {@code confirmUrl} to the user; the password is only generated and
 * changed once the user follows the link and confirms.
 *
 * @param email      the account a reset was requested for; never null
 * @param confirmUrl link to the confirmation page carrying the reset token
 */
public record PasswordResetRequestEvent(
        String email,
        String confirmUrl
) {
}
