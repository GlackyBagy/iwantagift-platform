package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.models.dto.AccountEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountProducer {

    private static final String ACCOUNT_EVENT_TOPIC = "accountEvent";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IwagProperties iwagProperties;

    public void sendOnCreate(UUID userId, String nickname, String email) {
        sendToTopic(ACCOUNT_EVENT_TOPIC, new AccountEvent(userId, nickname, email, AccountEvent.Type.CREATED));
    }

    public void sendOnDelete(UUID userId) {
        sendToTopic(ACCOUNT_EVENT_TOPIC, new AccountEvent(userId, null, null, AccountEvent.Type.DELETED));
    }

    private void sendToTopic(String topic, AccountEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTopics()
                .stream()
                .filter(topic::equals)
                .forEach(kafkaTopic -> kafkaTemplate.send(kafkaTopic, event.id().toString(), payload));
    }

    private Set<String> kafkaTopics() {
        return iwagProperties.getServices()
                .values()
                .stream()
                .map(IwagProperties.ServiceProperties::getKafkaTopics)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
