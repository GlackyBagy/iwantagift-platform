package online.iwantagift.api.profile.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.api.profile.models.dto.AccountEventDTO;
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
public class UserCreatedConsumer {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "createdUser", groupId = "profile")
    public void onUserCreated(String payload) {
        AccountEventDTO event = objectMapper.readValue(payload, AccountEventDTO.class);

        profileService.createProfileIfAbsent(event.id(), event.nickname());
        log.info("Ensured profile exists for user {}", event.id());
    }
}
