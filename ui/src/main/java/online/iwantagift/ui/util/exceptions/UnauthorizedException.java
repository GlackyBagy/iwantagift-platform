package online.iwantagift.ui.util.exceptions;

public class UnauthorizedException extends AuthServiceException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
