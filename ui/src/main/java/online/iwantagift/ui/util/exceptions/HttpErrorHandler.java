package online.iwantagift.ui.util.exceptions;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;

public class HttpErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        ResponseErrorHandler.super.handleError(url, method, response);
        String body = new String(response.getBody().readAllBytes());

        if (response.getStatusCode().value() == 401) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, body);
        }

        if (response.getStatusCode().value() == 409) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, body);
        }

        if (response.getStatusCode().value() == 400) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, body);
        }

        if (response.getStatusCode().value() == 404) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, body);
        }

        if (response.getStatusCode().value() == 503) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, body);
        }
        if (response.getStatusCode().is5xxServerError())
            throw new RemoteServiceException("Remote service error [URL: " + url + " ], body: " + body);

        throw new RemoteServiceException("4xx error: " + body);
    }
}
