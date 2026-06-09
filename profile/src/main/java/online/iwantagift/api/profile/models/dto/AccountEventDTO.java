package online.iwantagift.api.profile.models.dto;

import java.util.UUID;

/**
 * Account lifecycle event published by the auth service to Kafka.
 */
public record AccountEventDTO(UUID id, String nickname, String email) {
}
