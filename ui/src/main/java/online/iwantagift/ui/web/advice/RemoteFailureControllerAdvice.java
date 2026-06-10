package online.iwantagift.ui.web.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;

/**
 * Renders a friendly 503 page when a downstream service is unreachable.
 *
 * <p>When a downstream service is down (connection refused) or hangs past the configured timeout,
 * {@code RestClient} raises a {@link ResourceAccessException} — which is neither an HTTP-status
 * error ({@code RemoteServiceException}) nor a {@code ResponseStatusException}, so the page
 * controllers' catch blocks miss it and it would otherwise surface as a generic 500. This advice
 * maps it to the 503 page for all MVC page controllers.
 *
 * <p>It does not affect the BFF (REST) controllers: those go through {@code ProfileService}, which
 * already translates {@link ResourceAccessException} into a {@code ResponseStatusException(503)} and
 * returns a proper status to the client. Page controllers that intentionally degrade on this
 * exception (e.g. the profile page) catch it locally, so their handling takes precedence.
 */
@ControllerAdvice
@Slf4j
public class RemoteFailureControllerAdvice {

    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleServiceUnreachable(ResourceAccessException e) {
        log.warn("Downstream service unreachable: {}", e.getMessage());
        return "error/503";
    }
}
