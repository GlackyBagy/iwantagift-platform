package online.iwantagift.ui.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private static final String USER_ID_CLAIM = "userId";

    private final OAuth2AuthorizedClientService authorizedClientService;

    public UUID requireUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return UUID.fromString(oidcUser.getClaimAsString(USER_ID_CLAIM));
        }

        throw new IllegalStateException("Authenticated principal is not an OIDC user");
    }

    public String email(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }

        return authentication.getName();
    }

    public String requireAccessToken(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new IllegalStateException("Authentication is not OAuth2");
        }

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("OAuth2 access token is missing");
        }

        return client.getAccessToken().getTokenValue();
    }
}
