package online.iwantagift.api.profile.services;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.profile.models.entities.Avatar;
import online.iwantagift.api.profile.models.entities.Profile;
import online.iwantagift.api.profile.models.payloads.ProfilePayload;
import online.iwantagift.api.profile.repositories.AvatarRepository;
import online.iwantagift.api.profile.repositories.ProfileRepository;
import online.iwantagift.api.profile.services.storage.AvatarStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final long MAX_AVATAR_SIZE_BYTES = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ProfileRepository profileRepository;
    private final AvatarRepository avatarRepository;
    private final AvatarStorageService avatarStorageService;

    public Optional<Profile> getProfile(UUID profileId) {
        return profileRepository.findById(profileId);
    }

    /**
     * Creates a profile for a newly registered account (idempotent, driven by Kafka).
     */
    @Transactional
    public void createProfileIfAbsent(UUID profileId, String nickname) {
        if (profileRepository.existsById(profileId))
            return;

        profileRepository.save(Profile.builder()
                .id(profileId)
                .nickname(nickname != null && !nickname.isBlank() ? normalizeNickname(nickname) : "New user")
                .build());
    }

    public Optional<String> getAvatarPublicUrl(UUID profileId) {
        return profileRepository.findById(profileId)
                .map(Profile::getAvatar)
                .map(Avatar::getStorageKey)
                .map(avatarStorageService::publicUrl);
    }

    @Transactional
    public void updateProfile(UUID profileId, ProfilePayload payload) {
        Profile profile = profileRepository.findById(profileId)
                .orElseGet(() -> Profile.builder().id(profileId).build());

        if (payload.getNickname() != null)
            profile.setNickname(normalizeNickname(payload.getNickname()));

        if (payload.getDescription() != null)
            profile.setDescription(normalizeDescription(payload.getDescription()));

        if (payload.getProfilePhoto() != null && !payload.getProfilePhoto().isEmpty())
            profile.setAvatar(saveAvatar(profileId, payload.getProfilePhoto()));

        if (profile.getNickname() == null)
            profile.setNickname("New user"); // nickname will be set by kafka

        profileRepository.save(profile);
    }

    private Avatar saveAvatar(UUID profileId, MultipartFile profilePhoto) {
        validateAvatar(profilePhoto);

        UUID avatarId = UUID.randomUUID();
        String contentType = profilePhoto.getContentType();

        try {
            String storageKey = avatarStorageService.upload(
                    profileId,
                    avatarId,
                    contentType,
                    profilePhoto.getSize(),
                    profilePhoto.getInputStream()
            );

            Avatar avatar = Avatar.builder()
                    .id(avatarId)
                    .storageKey(storageKey)
                    .contentType(contentType)
                    .sizeBytes(profilePhoto.getSize())
                    .build();

            return avatarRepository.save(avatar);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read avatar file", e);
        }
    }

    private void validateAvatar(MultipartFile profilePhoto) {
        String contentType = profilePhoto.getContentType();

        if (contentType == null || !ALLOWED_AVATAR_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported avatar content type");
        }

        if (profilePhoto.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is too large");
        }
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname is required");
        }

        String normalized = nickname.trim();
        if (normalized.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname is too long");
        }

        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
