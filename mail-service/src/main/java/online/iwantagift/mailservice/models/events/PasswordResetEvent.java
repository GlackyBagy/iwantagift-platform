package online.iwantagift.mailservice.models.events;

public record PasswordResetEvent(
        String email,
        String newPassword
) {
}
