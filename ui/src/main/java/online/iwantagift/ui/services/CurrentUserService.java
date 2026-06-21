package online.iwantagift.ui.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.ui.util.exceptions.ReauthenticationRequiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private static final String USER_ID_CLAIM = "userId";

    private final OAuth2AuthorizedClientManager authorizedClientManager;

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

        // authorize() loads the persisted authorized client and, if the access token has expired,
        // transparently refreshes it via the stored refresh token and re-persists the result.
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(oauthToken.getAuthorizedClientRegistrationId())
                .principal(authentication)
                .build();

        OAuth2AuthorizedClient client;
        try {
            client = authorizedClientManager.authorize(authorizeRequest);
        } catch (OAuth2AuthorizationException ex) {
            // Refresh failed (typically invalid_grant: the auth server no longer recognizes the
            // stored refresh token). The local tokens are unusable — force the user back through
            // the login flow rather than surfacing a 500.
            throw new ReauthenticationRequiredException(
                    "Failed to obtain/refresh access token: " + ex.getError().getErrorCode(), ex);
        }

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("OAuth2 access token is missing");
        }

        return client.getAccessToken().getTokenValue();
    }
}
