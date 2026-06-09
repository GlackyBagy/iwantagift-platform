package online.iwantagift.api.wishlist.messaging.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.config.IwagProperties;
import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class NewWishProducer {
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    private final IwagProperties iwagProperties;

    private String topicName;

    @PostConstruct
    void init() {
        Set<String> topics = iwagProperties.getRequiredService("price").getKafkaTopics();
        if (topics.isEmpty()) {
            throw new IllegalStateException("No Kafka topics configured for price service");
        }

        topicName = topics.iterator().next();
    }

    public void send(WishCreateDTO dto) {
        template.send(topicName, objectMapper.writeValueAsString(dto));
    }
}
