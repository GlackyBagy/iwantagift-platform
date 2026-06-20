package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.models.events.AccountEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountProducer {

    private static final String ACCOUNT_EVENT_TOPIC = "accountEvent";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOnCreate(UUID userId, String nickname, String email) {
        sendToTopic(ACCOUNT_EVENT_TOPIC, new AccountEvent(userId, nickname, email, AccountEvent.Type.CREATED));
    }

    public void sendOnDelete(UUID userId) {
        sendToTopic(ACCOUNT_EVENT_TOPIC, new AccountEvent(userId, null, null, AccountEvent.Type.DELETED));
    }

    private void sendToTopic(String topic, AccountEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, event.id().toString(), payload);
    }
}
