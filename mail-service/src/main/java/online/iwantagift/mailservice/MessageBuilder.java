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

    private static final String ACCOUNT_DELETE_CONFIRM_SUBJECT = "Confirm your account deletion";
    private static final String ACCOUNT_DELETE_CONFIRM_TEMPLATE = "Open this link to continue deleting your account: %n%s";

    private static final String DATA_DELETE_CONFIRM_SUBJECT = "Confirm deletion of all data";
    private static final String DATA_DELETE_CONFIRM_TEMPLATE = "Open this link to confirm deletion of all wishlists and wishes: %n%s";

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

    public static SimpleMailMessage buildForAccountDeleteConfirm(String to, String confirmUrl){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(ACCOUNT_DELETE_CONFIRM_SUBJECT);
        message.setText(ACCOUNT_DELETE_CONFIRM_TEMPLATE.formatted(confirmUrl));
        return message;
    }

    public static SimpleMailMessage buildForDataDeleteConfirm(String to, String confirmUrl){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(DATA_DELETE_CONFIRM_SUBJECT);
        message.setText(DATA_DELETE_CONFIRM_TEMPLATE.formatted(confirmUrl));
        return message;
    }
}
