package online.iwantagift.ui.util.exceptions;

public class ServiceUnauthorizedException extends AuthServiceException {
    public ServiceUnauthorizedException(String message) {
        super(message);
    }
}
