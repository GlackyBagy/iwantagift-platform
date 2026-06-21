package online.iwantagift.api.wishlist.messaging.kafka;

import lombok.RequiredArgsConstructor;
import online.iwantagift.api.wishlist.models.events.AccountEvent;
import online.iwantagift.api.wishlist.services.WishlistService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AccountEventConsumer {
    private final WishlistService wlService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"accountEvent"})
    void handleAccountEvent(String jsonEvent) {
        AccountEvent event = objectMapper.readValue(jsonEvent, AccountEvent.class);

        switch (event.type()) {
            case CREATED -> wlService.getOrCreateDefaultList(event.id());
            case DELETED -> wlService.deleteAllByOwnerId(event.id());
        }
    }
}
