package online.iwantagift.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
public class EmailVerificationUrlBuilder {

    private final String authBaseUrl;

    public EmailVerificationUrlBuilder(@Value("${spring.security.oauth2.authorizationserver.issuer}")
                                       String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    public String buildForEmail(String token) {
        return buildForPathAndToken("/auth/verify/email", token);
    }

    public String buildForResetConfirm(String token) {
        return buildForPathAndToken("/auth/reset/password/confirm", token);
    }

    public String buildForAccountDeleteConfirm(String token) {
        return buildForPathAndToken("/auth/deleteAccount/confirm", token);
    }

    public String buildForDataDeleteConfirm(String token) {
        return buildForPathAndToken("/auth/deleteAll/confirm", token);
    }

    private String buildForPathAndToken(String path, String token) {
        return UriComponentsBuilder.fromUriString(authBaseUrl)
                .path(path)
                .queryParam("token", UriUtils.encode(token, StandardCharsets.UTF_8))
                .build(true)
                .toUriString();
    }
}
