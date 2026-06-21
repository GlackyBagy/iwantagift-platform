package online.iwantagift.auth.models.events;

/**
 * Emitted when a password reset is requested.
 *
 * <p>The account password has already been replaced with {@code newPassword} (stored hashed) by the
 * time this event is produced; the mail service only delivers the new plaintext password to the
 * user so they can sign in and change it.
 *
 * @param email       the account whose password was reset; never null
 * @param newPassword the freshly generated plaintext password to deliver to the user
 */
public record PasswordResetEvent(
        String email,
        String newPassword
) {
}
