package online.iwantagift.api.profile.util.exceptions;

public class ServiceUnauthorizedException extends RemoteServiceException {

    public ServiceUnauthorizedException(String message) {
        super(message);
    }
}
