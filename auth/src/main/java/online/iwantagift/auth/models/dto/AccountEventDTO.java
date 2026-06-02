package online.iwantagift.auth.models.dto;

import java.util.UUID;

public record AccountEventDTO(UUID id, String nickname, String email) {
}
