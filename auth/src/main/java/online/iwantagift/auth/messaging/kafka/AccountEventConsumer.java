package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.iwantagift.auth.models.events.AccountEvent;
import online.iwantagift.auth.repositories.AccountRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "accountEvent", groupId = "auth")
    void handleAccountEvent(String jsonEvent) {
        AccountEvent event = objectMapper.readValue(jsonEvent, AccountEvent.class);

        switch (event.type()) {
            case CREATED -> log.debug("Account created: {}", event.id());
            case DELETED -> {
                accountRepository.deleteById(event.id());
                log.info("Deleted account: {}", event.id());
            }
        }
    }
}
