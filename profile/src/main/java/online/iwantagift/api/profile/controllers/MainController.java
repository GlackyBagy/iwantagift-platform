package online.iwantagift.api.profile.controllers;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.profile.models.dto.ProfileDTO;
import online.iwantagift.api.profile.models.entities.Profile;
import online.iwantagift.api.profile.models.payloads.ProfilePayload;
import online.iwantagift.api.profile.services.ProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MainController {

    private final ProfileService profileService;

    @PostMapping(value = "/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> editProfile(@ModelAttribute @Validated ProfilePayload profilePayload,
                                            Authentication authentication) {
        UUID profileId = UUID.fromString(authentication.getName());
        profileService.updateProfile(profileId, profilePayload);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile(@RequestParam UUID profileId) {
        Optional<Profile> optional = profileService.getProfile(profileId);

        if (optional.isEmpty())
            return ResponseEntity.notFound().build();

        var profile = optional.get();
        var dto = new ProfileDTO(
                profile.getId(),
                profile.getNickname(),
                profile.getDescription(),
                profile.getAvatar() != null
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/profile/{profileId}/avatar")
    public ResponseEntity<Void> getAvatar(@PathVariable UUID profileId) {
        return profileService.getAvatarPublicUrl(profileId)
                .map(url -> ResponseEntity.status(302)
                        .location(URI.create(url))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
