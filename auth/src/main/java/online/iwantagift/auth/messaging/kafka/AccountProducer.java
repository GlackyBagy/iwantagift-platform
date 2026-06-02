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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountProducer {

    private static final String CREATED_USER_TOPIC = "createdUser";
    private static final String DELETED_USER_TOPIC = "deletedUser";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IwagProperties iwagProperties;

    public void sendOnCreate(Account account) {
        sendToTopic(CREATED_USER_TOPIC, account);
    }

    public void sendOnDelete(Account account) {
        sendToTopic(DELETED_USER_TOPIC, account);
    }

    private void sendToTopic(String topic, Account account) {
        String payload = objectMapper.writeValueAsString(AccountEventDTO.from(account));
        kafkaTopics()
                .stream()
                .filter(topic::equals)
                .forEach(kafkaTopic -> kafkaTemplate.send(kafkaTopic, account.getId().toString(), payload));
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
