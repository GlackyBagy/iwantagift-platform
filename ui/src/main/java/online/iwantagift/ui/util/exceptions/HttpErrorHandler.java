package online.iwantagift.ui.util.exceptions;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

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
            throw new ServiceUnauthorizedException(body);
        }

        if (response.getStatusCode().value() == 409) {
            throw new ConflictException(body);
        }

        if (response.getStatusCode().value() == 400) {
            throw new BadRequestException(body);
        }

        if (response.getStatusCode().value() == 404) {
            throw new NotFoundException(body);
        }

        if (response.getStatusCode().is5xxServerError())
            throw new RemoteServiceException("Remote service error [URL: " + url + " ], body: " + body);

        throw new RemoteServiceException("4xx error: " + body);
    }
}
