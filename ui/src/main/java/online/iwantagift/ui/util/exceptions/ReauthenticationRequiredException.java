package online.iwantagift.ui.util.exceptions;

/**
 * Raised when the current OAuth2 access token cannot be obtained or refreshed (e.g. the auth server
 * restarted and no longer recognizes the stored refresh token — {@code invalid_grant}). The user's
 * local session is still present but its tokens are unusable, so the only recovery is to send the
 * user back through the OAuth2 login flow.
 */
public class ReauthenticationRequiredException extends RuntimeException {
    public ReauthenticationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
