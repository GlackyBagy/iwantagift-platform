package online.iwantagift.api.wishlist.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.config.IwagProperties;
import online.iwantagift.api.wishlist.models.events.WishCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NewWishProducer {
    private static final String NEW_WISH_TOPIC = "newWishes";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IwagProperties iwagProperties;

    public void send(WishCreatedEvent event) {
        sendToTopic(NEW_WISH_TOPIC, event);
    }

    private void sendToTopic(String topic, WishCreatedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTopics()
                .stream()
                .filter(topic::equals)
                .forEach(kafkaTopic -> kafkaTemplate.send(kafkaTopic, event.wishId().toString(), payload));
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
