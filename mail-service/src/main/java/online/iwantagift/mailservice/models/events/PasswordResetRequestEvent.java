package online.iwantagift.mailservice.models.events;

public record PasswordResetRequestEvent(
        String email,
        String confirmUrl
) {
}
