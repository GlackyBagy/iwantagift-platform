package online.iwantagift.ui.util;

import online.iwantagift.ui.util.exceptions.AuthServiceException;
import online.iwantagift.ui.util.exceptions.BadRequestException;
import online.iwantagift.ui.util.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

public final class AuthErrorHandler {
    public static void handle(RestClient.ResponseSpec responseSpec) throws AuthServiceException {
        responseSpec
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());

                    if (response.getStatusCode().value() == 401) {
                        throw new UnauthorizedException(body);
                    }

                    if (response.getStatusCode().value() == 400) {
                        throw new BadRequestException(body);
                    }

                    throw new AuthServiceException("4xx error: " + body);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes());
                    throw new AuthServiceException("Auth service error: " + body);
                });
    }
}
