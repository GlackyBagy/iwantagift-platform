package online.iwantagift.ui.models.dto.profile;

import java.util.UUID;

public record ProfileDTO(UUID id, String nickname, String description, boolean hasAvatar) {
}
