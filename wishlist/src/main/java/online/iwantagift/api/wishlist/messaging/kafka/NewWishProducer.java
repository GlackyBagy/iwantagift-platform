package online.iwantagift.api.wishlist.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.dto.WishCreateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class NewWishProducer {
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;
    @Value("${iwag.services.price.kafka-topic}")
    private String topicName;

    public void send(WishCreateDTO dto) {
        template.send(topicName, objectMapper.writeValueAsString(dto));
    }
}
