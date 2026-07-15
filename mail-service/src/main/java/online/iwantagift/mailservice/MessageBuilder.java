package online.iwantagift.mailservice;

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

    public static MailMessage buildForUpdateEmail(String to, String verificationUrl) {
        return new MailMessage(to, UPDATE_EMAIL_SUBJECT, UPDATE_EMAIL_CONTENT_TEMPLATE.formatted(verificationUrl));
    }

    public static MailMessage buildForEmailVerification(String to, String verificationUrl) {
        return new MailMessage(to, VERIFY_EMAIL_SUBJECT, VERIFY_EMAIL_CONTENT_TEMPLATE.formatted(verificationUrl));
    }

    public static MailMessage buildForPasswordResetRequest(String to, String confirmUrl) {
        return new MailMessage(to, RESET_PASSWORD_REQUEST_SUBJECT, RESET_PASSWORD_REQUEST_CONTENT_TEMPLATE.formatted(confirmUrl));
    }

    public static MailMessage buildForPasswordReset(String to, String newPassword) {
        return new MailMessage(to, RESET_PASSWORD_SUBJECT, RESET_PASSWORD_CONTENT_TEMPLATE.formatted(newPassword));
    }

    public static MailMessage buildForAccountDeleteConfirm(String to, String confirmUrl) {
        return new MailMessage(to, ACCOUNT_DELETE_CONFIRM_SUBJECT, ACCOUNT_DELETE_CONFIRM_TEMPLATE.formatted(confirmUrl));
    }

    public static MailMessage buildForDataDeleteConfirm(String to, String confirmUrl) {
        return new MailMessage(to, DATA_DELETE_CONFIRM_SUBJECT, DATA_DELETE_CONFIRM_TEMPLATE.formatted(confirmUrl));
    }
}
