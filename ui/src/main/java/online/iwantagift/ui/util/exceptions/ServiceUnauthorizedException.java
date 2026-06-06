package online.iwantagift.ui.util.exceptions;

@Deprecated
public class ServiceUnauthorizedException extends RemoteServiceException {
    public ServiceUnauthorizedException(String message) {
        super(message);
    }
}
