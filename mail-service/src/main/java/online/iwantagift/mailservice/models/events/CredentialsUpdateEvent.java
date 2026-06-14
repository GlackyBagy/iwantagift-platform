package online.iwantagift.mailservice.models.events;


public record CredentialsUpdateEvent(
        String oldEmail,
        String newEmail,
        String verificationUrl
) {
}
