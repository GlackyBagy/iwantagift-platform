package online.iwantagift.ui.web.controllers.bff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.ui.models.dto.profile.ProfileDTO;
import online.iwantagift.ui.services.CurrentUserService;
import online.iwantagift.ui.services.ProfileService;
import online.iwantagift.ui.util.exceptions.RemoteServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Backend-for-frontend (BFF) endpoints for the profile section.
 *
 * <p>These are called by the browser (via fetch) after the page shell has already rendered, so a
 * slow or unavailable profile service degrades only the profile widget instead of failing the whole
 * page. The ui session cookie authenticates the call and the access token never leaves the server.
 *
 * <p>A missing profile record (404) is downgraded to defaults by {@link ProfileService}, so it is
 * returned as a normal 200. A genuinely unavailable profile service surfaces as a non-2xx status,
 * which the client uses to disable the profile block.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileBffController {
    private static final String DEFAULT_PROFILE_DESCRIPTION = "Profile description is not set yet.";
    private static final String FALLBACK_AVATAR_URL = "/img/logo_load_error.png";

    private final CurrentUserService currentUserService;
    private final ProfileService profileService;

    @GetMapping
    public ProfileView currentProfile(Authentication authentication) {
        UUID userId = currentUserService.requireUserId(authentication);

        // May throw ResponseStatusException(503) / RemoteServiceException when profile is down;
        // both surface as a non-2xx so the client treats the widget as unavailable.
        ProfileDTO profile = profileService.getProfile(userId);

        String description = profile.description() != null
                ? profile.description()
                : DEFAULT_PROFILE_DESCRIPTION;
        String avatarUrl = profile.hasAvatar()
                ? profileService.avatarUrl(userId)
                : FALLBACK_AVATAR_URL;

        return new ProfileView(
                currentUserService.email(authentication),
                profile.nickname(),
                description,
                avatarUrl);
    }

    /**
     * Public profile of any user by id, used to hydrate the profile-page hero (own and foreign).
     * Returns only public fields (no email). Behaves like {@link #currentProfile}: a missing record
     * (404) is downgraded to defaults, an unavailable service surfaces as a non-2xx.
     */
    @GetMapping("/{profileOwnerId}")
    public PublicProfileView profileById(@PathVariable UUID profileOwnerId) {
        ProfileDTO profile = profileService.getProfile(profileOwnerId);

        String description = profile.description() != null
                ? profile.description()
                : DEFAULT_PROFILE_DESCRIPTION;
        String avatarUrl = profile.hasAvatar()
                ? profileService.avatarUrl(profileOwnerId)
                : FALLBACK_AVATAR_URL;

        return new PublicProfileView(profile.nickname(), description, avatarUrl);
    }

    /**
     * Generic remote 5xx failures map to 503 so the client treats the widget as temporarily
     * unavailable. {@link org.springframework.web.server.ResponseStatusException} already carries
     * its own status and is handled by Spring's default resolver.
     */
    @ExceptionHandler(RemoteServiceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleRemoteFailure(RemoteServiceException e) {
        log.warn("Profile BFF unavailable: {}", e.getMessage());
    }

    /** Shape consumed by the settings page profile widget (current user, includes email). */
    public record ProfileView(String email, String nickname, String description, String avatarUrl) {
    }

    /** Public profile shape consumed by the profile-page hero (no email). */
    public record PublicProfileView(String nickname, String description, String avatarUrl) {
    }
}
