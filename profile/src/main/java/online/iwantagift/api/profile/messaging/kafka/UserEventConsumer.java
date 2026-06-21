package online.iwantagift.api.profile.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.api.profile.models.dto.AccountEvent;
import online.iwantagift.api.profile.services.ProfileService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Creates a profile record for every account registered in the auth service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "accountEvent", groupId = "profile")
    public void handleAccountEvent(String payload) {
        AccountEvent event = objectMapper.readValue(payload, AccountEvent.class);

        switch (event.type()) {
            case CREATED -> {
                profileService.createProfileIfAbsent(event.id(), event.nickname());
                log.info("Ensured profile exists for user {}", event.id());
            }
            case DELETED -> {
                profileService.deleteProfileById(event.id());
                log.info("Idempotently deleted profile for user {}", event.id());
            }
        }
    }
}
