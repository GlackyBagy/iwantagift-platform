package online.iwantagift.mailservice.models.events;

public record MailRequestEvent(
        String email,
        String confirmUrl
) {
}
