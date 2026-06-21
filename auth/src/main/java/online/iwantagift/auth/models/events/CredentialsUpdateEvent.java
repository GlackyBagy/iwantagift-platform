package online.iwantagift.auth.models.events;

/**
 *
 * @param oldEmail never null
 * @param newEmail ignored when {@code type != CHANGE_EMAIL}
 * @param verificationUrl URL sent to the user to confirm the credentials update
 */
public record CredentialsUpdateEvent(
        String oldEmail,
        String newEmail,
        String verificationUrl

) {
}
