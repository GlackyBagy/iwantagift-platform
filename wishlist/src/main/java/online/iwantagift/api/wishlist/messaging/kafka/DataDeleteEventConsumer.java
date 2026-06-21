package online.iwantagift.api.wishlist.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.events.DataDeleteEvent;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DataDeleteEventConsumer {

    public static final String DATA_DELETE_TOPIC = "deleteAll";

    private final WishlistService wlService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = DATA_DELETE_TOPIC)
    void handleDeleteAllEvent(String jsonEvent){
        DataDeleteEvent event = objectMapper.readValue(jsonEvent, DataDeleteEvent.class);
        wlService.deleteAllByOwnerId(event.userId());
    }
}
