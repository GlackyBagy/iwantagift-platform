package online.iwantagift.auth.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.auth.config.IwagProperties;
import online.iwantagift.auth.models.dto.AccountEventDTO;
import online.iwantagift.auth.models.entities.Account;
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

    private static final String CREATED_USER_TOPIC = "createdUser";
    private static final String DELETED_USER_TOPIC = "deletedUser";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IwagProperties iwagProperties;

    public void sendOnCreate(UUID userId, String nickname, String email) {
        sendToTopic(CREATED_USER_TOPIC, new AccountEventDTO(userId, nickname, email));
    } // todo send nickname

    public void sendOnDelete(UUID userId) {
        sendToTopic(DELETED_USER_TOPIC, new AccountEventDTO(userId, null, null));
    }

    private void sendToTopic(String topic, AccountEventDTO event) {
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
