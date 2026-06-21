package online.iwantagift.ui.models.dto.auth;

import java.util.UUID;

public record AccountDTO(UUID id, String nickname, String email) {
}
