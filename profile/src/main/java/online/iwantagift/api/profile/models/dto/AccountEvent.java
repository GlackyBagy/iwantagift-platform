package online.iwantagift.api.profile.models.dto;

import java.util.UUID;

/**
 * Account lifecycle event published by the auth service to Kafka.
 */
public record AccountEvent(UUID id, String nickname, String email, Type type) {
    public enum Type {
        CREATED,
        DELETED
    }
}
