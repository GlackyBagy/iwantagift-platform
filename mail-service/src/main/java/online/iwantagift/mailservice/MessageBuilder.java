package online.iwantagift.mailservice;

import org.springframework.mail.SimpleMailMessage;

public class MessageBuilder {
    private static final String UPDATE_EMAIL_SUBJECT = "Update email";
    private static final String UPDATE_EMAIL_CONTENT_TEMPLATE = "Here is your link to confirm changing your email: %n%s";

    private static final String VERIFY_EMAIL_SUBJECT = "Verify your email";
    private static final String VERIFY_EMAIL_CONTENT_TEMPLATE = "Here is your link to verify current email: %n%s";

    private static final String RESET_PASSWORD_REQUEST_SUBJECT = "Confirm your password reset";
    private static final String RESET_PASSWORD_REQUEST_CONTENT_TEMPLATE =
            "A password reset was requested for your account. " +
                    "Follow this link to confirm and receive a new password: %n%s%n%n" +
                    "If you did not request this, you can ignore this email.";

    private static final String RESET_PASSWORD_SUBJECT = "Password reset";
    private static final String RESET_PASSWORD_CONTENT_TEMPLATE =
            "Here is your new password: %n%s%n%nPlease sign in and change it as soon as possible.";

    public static SimpleMailMessage buildForUpdateEmail(String to, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(UPDATE_EMAIL_SUBJECT);
        message.setText(UPDATE_EMAIL_CONTENT_TEMPLATE.formatted(verificationUrl));
        return message;
    }

    public static SimpleMailMessage buildForEmailVerification(String to, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(VERIFY_EMAIL_SUBJECT);
        message.setText(VERIFY_EMAIL_CONTENT_TEMPLATE.formatted(verificationUrl));
        return message;
    }

    public static SimpleMailMessage buildForPasswordResetRequest(String to, String confirmUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(RESET_PASSWORD_REQUEST_SUBJECT);
        message.setText(RESET_PASSWORD_REQUEST_CONTENT_TEMPLATE.formatted(confirmUrl));
        return message;
    }

    public static SimpleMailMessage buildForPasswordReset(String to, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(RESET_PASSWORD_SUBJECT);
        message.setText(RESET_PASSWORD_CONTENT_TEMPLATE.formatted(newPassword));
        return message;
    }
}
