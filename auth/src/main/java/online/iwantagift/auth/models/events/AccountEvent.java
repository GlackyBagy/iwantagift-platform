package online.iwantagift.auth.models.events;

import java.util.UUID;


public record AccountEvent(UUID id, String nickname, String email, Type type) {
    public enum Type {
        CREATED,
        DELETED
    }
}
